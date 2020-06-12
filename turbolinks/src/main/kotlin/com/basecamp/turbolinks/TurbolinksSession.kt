package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.util.SparseArray
import android.webkit.*
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature.*
import com.basecamp.turbolinks.VisitAction.*
import kotlinx.coroutines.launch
import java.util.*

@Suppress("unused")
class TurbolinksSession private constructor(val sessionName: String, val activity: Activity, val webView: TurbolinksWebView) {
    internal lateinit var currentVisit: TurbolinksVisit
    internal var coldBootVisitIdentifier = ""
    internal var previousOverrideUrlTime = 0L
    internal var visitPending = false
    internal var isRenderProcessGone = false
    internal var restorationIdentifiers = SparseArray<String>()
    internal val httpRepository = TurbolinksHttpRepository()

    // User accessible

    val context: Context = activity.applicationContext
    var rootLocation: String? = null
    var pathConfiguration = PathConfiguration(context)
    var offlineRequestHandler: TurbolinksOfflineRequestHandler? = null
    var enableScreenshots = true
    var isColdBooting = false
        internal set
    var isReady = false
        internal set

    init {
        initializeWebView()
        TurbolinksHttpClient.enableCachingWith(context)
    }

    // Public

    fun preCacheLocation(location: String) {
        val requestHandler = checkNotNull(offlineRequestHandler) {
            "An offline request handler must be provided to pre-cache $location"
        }

        activity.coroutineScope().launch {
            httpRepository.preCache(requestHandler, TurbolinksPreCacheRequest(
                url = location, userAgent = webView.settings.userAgentString
            ))
        }
    }

    fun reset() {
        if (::currentVisit.isInitialized) {
            logEvent("reset")
            currentVisit.identifier = ""
            coldBootVisitIdentifier = ""
            restorationIdentifiers.clear()
            visitPending = false
            isReady = false
            isColdBooting = false
        }
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        TurbolinksLog.enableDebugLogging = enabled
    }

    // Internal

    internal fun visit(visit: TurbolinksVisit) {
        this.currentVisit = visit
        callback { it.visitLocationStarted(visit.location) }

        if (visit.reload) {
            reset()
        }

        when {
            isColdBooting -> visitPending = true
            isReady -> visitLocation(visit)
            else -> visitLocationAsColdBoot(visit)
        }
    }


    // Callbacks from Turbolinks Core

    @JavascriptInterface
    fun visitProposedToLocation(location: String, optionsJson: String) {
        val options = VisitOptions.fromJSON(optionsJson) ?: return

        logEvent("visitProposedToLocation", "location" to location, "options" to options)
        callback { it.visitProposedToLocation(location, options) }
    }

    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String) {
        logEvent("visitStarted", "location" to location,
                "visitIdentifier" to visitIdentifier,
                "visitHasCachedSnapshot" to visitHasCachedSnapshot)

        currentVisit.identifier = visitIdentifier
    }

    @JavascriptInterface
    fun visitRequestCompleted(visitIdentifier: String) {
        logEvent("visitRequestCompleted", "visitIdentifier" to visitIdentifier)
    }

    @JavascriptInterface
    fun visitRequestFailedWithStatusCode(visitIdentifier: String, statusCode: Int) {
        logEvent("visitRequestFailedWithStatusCode",
                "visitIdentifier" to visitIdentifier,
                "statusCode" to statusCode)

        if (visitIdentifier == currentVisit.identifier) {
            callback { it.requestFailedWithStatusCode(statusCode) }
        }
    }

    @JavascriptInterface
    fun visitRequestFinished(visitIdentifier: String) {
        logEvent("visitRequestFinished", "visitIdentifier" to visitIdentifier)
    }

    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        logEvent("pageLoaded", "restorationIdentifier" to restorationIdentifier)
        restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
    }

    @JavascriptInterface
    fun visitRendered(visitIdentifier: String) {
        logEvent("visitRendered", "visitIdentifier" to visitIdentifier)

        if (visitIdentifier == coldBootVisitIdentifier || visitIdentifier == currentVisit.identifier) {
            if (isFeatureSupported(VISUAL_STATE_CALLBACK)) {
                postVisitVisualStateCallback(visitIdentifier)
            } else {
                callback { it.visitRendered() }
            }
        }
    }

    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String, restorationIdentifier: String) {
        logEvent("visitCompleted",
            "visitIdentifier" to visitIdentifier,
            "restorationIdentifier" to restorationIdentifier)

        if (visitIdentifier == currentVisit.identifier) {
            restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
            callback { it.visitCompleted(currentVisit.completedOffline) }
        }
    }

    @JavascriptInterface
    fun pageInvalidated() {
        logEvent("pageInvalidated")

        callback {
            it.pageInvalidated()
            visit(currentVisit.copy(reload = true))
        }
    }

    @JavascriptInterface
    fun turbolinksIsReady(isReady: Boolean) {
        logEvent("turbolinksIsReady", "isReady" to isReady)
        this.isReady = isReady
        this.isColdBooting = false

        if (!isReady) {
            reset()
            visitRequestFailedWithStatusCode(currentVisit.identifier, 500)
            return
        }

        // Check if a visit was requested while cold
        // booting. If so, visit the pending location.
        when (visitPending) {
            true -> visitPendingLocation(currentVisit)
            else -> renderVisitForColdBoot()
        }
    }

    @JavascriptInterface
    fun turbolinksFailedToLoad() {
        logEvent("turbolinksFailedToLoad")
        reset()
        callback { it.onReceivedError(-1) }
    }

    // Private

    private fun visitLocation(visit: TurbolinksVisit) {
        val restorationIdentifier = when (visit.options.action) {
            RESTORE -> restorationIdentifiers[visit.destinationIdentifier] ?: ""
            ADVANCE -> ""
            else -> ""
        }

        val options = when (restorationIdentifier) {
            "" -> visit.options.copy(action = ADVANCE)
            else -> visit.options
        }

        logEvent("visitLocation",
                "location" to visit.location,
                "options" to options,
                "restorationIdentifier" to restorationIdentifier)

        webView.visitLocation(visit.location, options, restorationIdentifier)
    }

    private fun visitLocationAsColdBoot(visit: TurbolinksVisit) {
        logEvent("visitLocationAsColdBoot", "location" to visit.location)
        isColdBooting = true

        // When a page is invalidated by Turbolinks, we need to reload the
        // same URL in the WebView. For a URL with an anchor, the WebView
        // sees a WebView.loadUrl() request as a same-page visit instead of
        // requesting a full page reload. To work around this, we call
        // WebView.reload(), which fully reloads the page for all URLs.
        when (visit.reload) {
            true -> webView.reload()
            else -> webView.loadUrl(visit.location)
        }
    }

    private fun visitPendingLocation(visit: TurbolinksVisit) {
        logEvent("visitPendingLocation", "location" to visit.location)
        visitLocation(visit)
        visitPending = false
    }

    private fun renderVisitForColdBoot() {
        logEvent("renderVisitForColdBoot", "coldBootVisitIdentifier" to coldBootVisitIdentifier)
        webView.visitRenderedForColdBoot(coldBootVisitIdentifier)
        callback { it.visitCompleted(currentVisit.completedOffline) }
    }

    /**
     * Updates to the DOM are processed asynchronously, so the changes may not immediately
     * be reflected visually by subsequent WebView.onDraw invocations. Use a VisualStateCallback
     * to be notified when the contents of the DOM are ready to be drawn.
     */
    private fun postVisitVisualStateCallback(visitIdentifier: String) {
        if (!isFeatureSupported(VISUAL_STATE_CALLBACK)) return

        context.runOnUiThread {
            WebViewCompat.postVisualStateCallback(webView, visitIdentifier.toRequestId()) { requestId ->
                logEvent("visitVisualStateComplete", "visitIdentifier" to visitIdentifier)

                if (visitIdentifier.toRequestId() == requestId) {
                    webView.requestLayout()
                    callback { it.visitRendered() }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        logEvent("WebView info",
            "package" to (webView.packageName ?: ""),
            "version" to (webView.versionName ?: ""),
            "major version" to (webView.majorVersion ?: ""))

        webView.apply {
            addJavascriptInterface(this@TurbolinksSession, "TurbolinksSession")
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
        }
    }

    private fun installBridge(location: String) {
        logEvent("installBridge", "location" to location)

        webView.installBridge {
            callback { it.onPageFinished(location) }
        }
    }

    private fun String.toRequestId(): Long {
        return hashCode().toLong()
    }

    private fun callback(action: (TurbolinksSessionCallback) -> Unit) {
        context.runOnUiThread {
            if (currentVisit.callback.isActive()) {
                action(currentVisit.callback)
            }
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply { add(0, "session" to sessionName) }
        logEvent(event, attributes)
    }


    // Classes and objects

    private inner class TurbolinksWebViewClient : WebViewClientCompat() {
        override fun onPageStarted(view: WebView, location: String, favicon: Bitmap?) {
            logEvent("onPageStarted", "location" to location)
            callback { it.onPageStarted(location) }
            coldBootVisitIdentifier = ""
        }

        override fun onPageFinished(view: WebView, location: String) {
            if (coldBootVisitIdentifier == location.identifier()) {
                // If we got here, onPageFinished() has already been called for
                // this location so bail. It's common for onPageFinished()
                // to be called multiple times when the document has initially
                // loaded and then when resources like images finish loading.
                return
            }

            if (!isColdBooting) {
                // We can get here even when the page failed to load. If
                // onReceivedError() or onReceivedHttpError() are called,
                // they reset the session, so we're no longer cold booting.
                return
            }

            logEvent("onPageFinished", "location" to location, "progress" to view.progress)
            coldBootVisitIdentifier = location.identifier()
            installBridge(location)
        }

        override fun onPageCommitVisible(view: WebView, location: String) {
            super.onPageCommitVisible(view, location)
            logEvent("onPageCommitVisible", "location" to location, "progress" to view.progress)
        }

        /**
         * Turbolinks will not call adapter.visitProposedToLocation in some cases,
         * like target=_blank or when the domain doesn't match. We still route those here.
         * This is only called when links within a webView are clicked and during a
         * redirect while cold booting.
         * http://stackoverflow.com/a/6739042/3280911
         */
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val location = request.url.toString()
            val isColdBootRedirect = isColdBooting && currentVisit.location != location
            val shouldOverride = isReady || isColdBootRedirect

            // Don't allow onPageFinished to process its
            // callbacks if a cold boot was blocked.
            if (isColdBootRedirect) {
                logEvent("coldBootRedirect", "location" to location)
                reset()
            }

            if (shouldOverride && shouldProposeThrottledVisit()) {
                // Replace the cold boot destination on a redirect
                // since the original url isn't visitable.
                val options = when (isColdBootRedirect) {
                    true -> VisitOptions(action = REPLACE)
                    else -> VisitOptions(action = ADVANCE)
                }
                visitProposedToLocation(location, options.toJson())
            }

            logEvent("shouldOverrideUrlLoading", "location" to location, "shouldOverride" to shouldOverride)
            return shouldOverride
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            callback { it.onReceivedHttpAuthRequest(handler, host, realm) }
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            // TODO: There are certain situations where shouldInterceptRequest will be called without a
            // true visit preceding it — specifically, requests outside of the main frame. We pass
            // these back to the app in case we want to handle them
            if (!request.isForMainFrame) {
                callback { it.onNonMainFrameRequest(request.url.toString()) }
            }

            val requestHandler = offlineRequestHandler ?: return null

            if (!request.method.equals("GET", ignoreCase = true) ||
                request.url.scheme?.startsWith("HTTP", ignoreCase = true) != true) {
                return null
            }

            val url = request.url.toString()
            val result = httpRepository.fetch(requestHandler, request)

            if (currentVisit.location == url) {
                currentVisit.completedOffline = result.offline
            }

            return result.response
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat) {
            super.onReceivedError(view, request, error)

            if (request.isForMainFrame && isFeatureSupported(WEB_RESOURCE_ERROR_GET_CODE)) {
                logEvent("onReceivedError", "errorCode" to error.errorCode)
                reset()
                callback { it.onReceivedError(error.errorCode) }
            }
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)

            if (request.isForMainFrame) {
                logEvent("onReceivedHttpError", "statusCode" to errorResponse.statusCode)
                reset()
                callback { it.onReceivedError(errorResponse.statusCode) }
            }
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            super.onReceivedSslError(view, handler, error)
            handler.cancel()

            logEvent("onReceivedSslError", "url" to error.url)
            reset()
            callback { it.onReceivedError(-1) }
        }

        @TargetApi(Build.VERSION_CODES.O)
        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            logEvent("onRenderProcessGone", "didCrash" to detail.didCrash())

            if (view == webView) {
                // Set a flag if the WebView render process is gone so we
                // can avoid using this session any further in the app.
                isRenderProcessGone = true

                // We can reach this callback even if a WebView visit hasn't been
                // performed yet. Guard against this state so we don't crash.
                if (::currentVisit.isInitialized) {
                    callback { it.onRenderProcessGone() }
                }
            }

            return true
        }

        /**
         * Prevents firing twice in a row within a few milliseconds of each other, which
         * happens sometimes. So we check for a slight delay between requests, which is
         * plenty of time to allow for a user to click the same link again.
         */
        private fun shouldProposeThrottledVisit(): Boolean {
            val limit = 500
            val currentTime = Date().time

            return (currentTime - previousOverrideUrlTime > limit).also {
                previousOverrideUrlTime = currentTime
            }
        }

        private fun String.identifier(): String {
            return hashCode().toString()
        }
    }

    companion object {
        fun getNew(sessionName: String, activity: Activity, webView: TurbolinksWebView): TurbolinksSession {
            return TurbolinksSession(sessionName, activity, webView)
        }
    }
}
