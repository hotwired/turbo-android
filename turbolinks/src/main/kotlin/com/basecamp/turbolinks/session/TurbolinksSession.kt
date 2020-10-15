package com.basecamp.turbolinks.session

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
import com.basecamp.turbolinks.config.TurbolinksPathConfiguration
import com.basecamp.turbolinks.config.screenshotsEnabled
import com.basecamp.turbolinks.http.TurbolinksHttpClient
import com.basecamp.turbolinks.http.TurbolinksHttpRepository
import com.basecamp.turbolinks.http.TurbolinksOfflineRequestHandler
import com.basecamp.turbolinks.http.TurbolinksPreCacheRequest
import com.basecamp.turbolinks.util.*
import com.basecamp.turbolinks.views.TurbolinksWebView
import com.basecamp.turbolinks.visit.TurbolinksVisit
import com.basecamp.turbolinks.visit.TurbolinksVisitAction
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions
import kotlinx.coroutines.launch
import java.util.*

/**
 * This class is primarily responsible for managing an instance of an Android WebView that will
 * be shared between fragments. An implementation of [TurbolinksSessionNavHostFragment] will create
 * a session for you and it can be retrieved via [TurbolinksSessionNavHostFragment.session].
 *
 * @property sessionName An arbitrary name to be used as an identifier for a given session.
 * @property activity The activity to which the session will be bound to.
 * @property webView An instance of a [TurbolinksWebView] to be shared/managed.
 * @constructor Create empty Turbolinks session
 */
@Suppress("unused")
class TurbolinksSession private constructor(internal val sessionName: String, internal val activity: Activity, val webView: TurbolinksWebView) {
    internal lateinit var currentVisit: TurbolinksVisit
    internal var coldBootVisitIdentifier = ""
    internal var previousOverrideUrlTime = 0L
    internal var visitPending = false
    internal var isRenderProcessGone = false
    internal var restorationIdentifiers = SparseArray<String>()
    internal val httpRepository = TurbolinksHttpRepository()
    internal val context: Context = activity.applicationContext
    internal var rootLocation: String? = null
    internal var isColdBooting = false

    // User accessible

    /**
     * Sets/gets the path configuration for this session. Default is an empty configuration.
     */
    var pathConfiguration = TurbolinksPathConfiguration(context)

    /**
     * Sets/gets the [TurbolinksOfflineRequestHandler] for this session. Default is `null`.
     */
    var offlineRequestHandler: TurbolinksOfflineRequestHandler? = null

    /**
     * Enables/disables transitional screenshots for this session. Default is `true`.
     */
    var enableScreenshots = pathConfiguration.settings.screenshotsEnabled


    /**
     * Provides the status of whether Turbolinks is initialized and ready for use.
     */
    var isReady = false
        internal set

    init {
        initializeWebView()
        TurbolinksHttpClient.enableCachingWith(context)
    }

    // Public

    /**
     * Fetches a given location and returns the response to the [TurbolinksOfflineRequestHandler]
     * Allows an offline cache to contain specific items instead of solely relying on visited items.
     *
     * @param location Location to cache.
     */
    fun preCacheLocation(location: String) {
        val requestHandler = checkNotNull(offlineRequestHandler) {
            "An offline request handler must be provided to pre-cache $location"
        }

        activity.coroutineScope().launch {
            httpRepository.preCache(
                requestHandler, TurbolinksPreCacheRequest(
                    url = location, userAgent = webView.settings.userAgentString
                )
            )
        }
    }

    /**
     * Resets this session to a cold boot state. The first subsequent visit after resetting will
     * execute a full cold boot (reloading of all resources).
     *
     */
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

    /**
     * Enables/disables debug logging. Disabled by default.
     *
     * @param enabled Whether to enable debug logging.
     */
    fun setDebugLoggingEnabled(enabled: Boolean) {
        TurbolinksLog.enableDebugLogging = enabled
        TurbolinksHttpClient.reset()
    }

    // Internal

    /**
     * Visit
     *
     * @param visit
     */
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

    /**
     * Remove callback
     *
     * @param callback
     */
    internal fun removeCallback(callback: TurbolinksSessionCallback) {
        if (currentVisit.callback == callback) {
            currentVisit.callback = null
        }
    }

    // Callbacks from Turbolinks Core

    /**
     * Called by Turbolinks when a new visit is initiated.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param location The location to visit.
     * @param optionsJson A JSON block to be serialzed into [TurbolinksVisitOptions].
     */
    @JavascriptInterface
    fun visitProposedToLocation(location: String, optionsJson: String) {
        val options = TurbolinksVisitOptions.fromJSON(optionsJson) ?: return

        logEvent("visitProposedToLocation", "location" to location, "options" to options)
        callback { it.visitProposedToLocation(location, options) }
    }

    /**
     * Called by Turbolinks when a new visit has just started.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param visitIdentifier A unique identifier for the visit.
     * @param visitHasCachedSnapshot Whether the visit has a cached snapshot available.
     * @param location The location being visited.
     */
    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String) {
        logEvent(
            "visitStarted", "location" to location,
            "visitIdentifier" to visitIdentifier,
            "visitHasCachedSnapshot" to visitHasCachedSnapshot
        )

        currentVisit.identifier = visitIdentifier
    }

    /**
     * Called by Turbolinks when the HTTP request has been completed.
     *
     * @param visitIdentifier A unique identifier for the visit.
     */
    @JavascriptInterface
    fun visitRequestCompleted(visitIdentifier: String) {
        logEvent("visitRequestCompleted", "visitIdentifier" to visitIdentifier)
    }

    /**
     * Called by Turbolinks when the HTTP request has failed.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param visitIdentifier A unique identifier for the visit.
     * @param visitHasCachedSnapshot Whether the visit has a cached snapshot available.
     * @param statusCode The HTTP status code that caused the failure.
     */
    @JavascriptInterface
    fun visitRequestFailedWithStatusCode(visitIdentifier: String, visitHasCachedSnapshot: Boolean, statusCode: Int) {
        logEvent(
            "visitRequestFailedWithStatusCode",
            "visitIdentifier" to visitIdentifier,
            "visitHasCachedSnapshot" to visitHasCachedSnapshot,
            "statusCode" to statusCode
        )

        if (visitIdentifier == currentVisit.identifier) {
            callback { it.requestFailedWithStatusCode(visitHasCachedSnapshot, statusCode) }
        }
    }

    /**
     * Called by Turbolinks when the HTTP request has been completed.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param visitIdentifier A unique identifier for the visit.
     */
    @JavascriptInterface
    fun visitRequestFinished(visitIdentifier: String) {
        logEvent("visitRequestFinished", "visitIdentifier" to visitIdentifier)
    }

    /**
     * Called by Turbolinks once the page has been fully loaded by the WebView.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param restorationIdentifier A unique identifier for restoring the page and scroll position
     * from cache.
     */
    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        logEvent("pageLoaded", "restorationIdentifier" to restorationIdentifier)
        restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
    }

    /**
     * Called by Turbolinks once the page has been fully rendered in the webView.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param visitIdentifier A unique identifier for the visit.
     */
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

    /**
     * Called by Turbolinks when the visit is fully completed (request successful and page rendered).
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param visitIdentifier  A unique identifier for the visit.
     * @param restorationIdentifier A unique identifier for restoring the page and scroll position
     * from cache.
     */
    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String, restorationIdentifier: String) {
        logEvent(
            "visitCompleted",
            "visitIdentifier" to visitIdentifier,
            "restorationIdentifier" to restorationIdentifier
        )

        if (visitIdentifier == currentVisit.identifier) {
            restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
            callback { it.visitCompleted(currentVisit.completedOffline) }
        }
    }

    /**
     * Called when Turbolinks detects that the page being visited has been invalidated, typically by
     * new resources in the the page HEAD.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     */
    @JavascriptInterface
    fun pageInvalidated() {
        logEvent("pageInvalidated")

        callback {
            it.pageInvalidated()
            visit(currentVisit.copy(reload = true))
        }
    }

    /**
     * Sets internal flags that indicate whether Turbolinks in the WebView is ready for use.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     *
     * @param isReady
     */
    @JavascriptInterface
    fun turbolinksIsReady(isReady: Boolean) {
        logEvent("turbolinksIsReady", "isReady" to isReady)
        this.isReady = isReady
        this.isColdBooting = false

        if (!isReady) {
            reset()
            visitRequestFailedWithStatusCode(currentVisit.identifier, false, 0)
            return
        }

        // Check if a visit was requested while cold
        // booting. If so, visit the pending location.
        when (visitPending) {
            true -> visitPendingLocation(currentVisit)
            else -> renderVisitForColdBoot()
        }
    }

    /**
     * Sets internal flags indicating that Turbolinks did not properly initialize.
     *
     * Warning: This method is public so it can be used as a Javascript Interface.
     * You should never call this directly as it could lead to unintended behavior.
     */
    @JavascriptInterface
    fun turbolinksFailedToLoad() {
        logEvent("turbolinksFailedToLoad")
        reset()
        callback { it.onReceivedError(-1) }
    }

    // Private

    private fun visitLocation(visit: TurbolinksVisit) {
        val restorationIdentifier = when (visit.options.action) {
            TurbolinksVisitAction.RESTORE -> restorationIdentifiers[visit.destinationIdentifier] ?: ""
            TurbolinksVisitAction.ADVANCE -> ""
            else -> ""
        }

        val options = when (restorationIdentifier) {
            "" -> visit.options.copy(action = TurbolinksVisitAction.ADVANCE)
            else -> visit.options
        }

        logEvent(
            "visitLocation",
            "location" to visit.location,
            "options" to options,
            "restorationIdentifier" to restorationIdentifier
        )

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
        logEvent(
            "WebView info",
            "package" to (webView.packageName ?: ""),
            "version" to (webView.versionName ?: ""),
            "major version" to (webView.majorVersion ?: "")
        )

        webView.apply {
            addJavascriptInterface(this@TurbolinksSession, "TurbolinksSession")
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
            initDownloadListener()
        }
    }

    private fun WebView.initDownloadListener() {
        setDownloadListener { url, _, _, _, _ ->
            logEvent("downloadListener", "location" to url)
            visitProposedToLocation(url, TurbolinksVisitOptions().toJson())
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
            currentVisit.callback?.let {
                if (it.isActive()) action(it)
            }
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply { add(0, "session" to sessionName) }
        logEvent(event, attributes)
    }


    // Classes and objects

    private inner class TurbolinksWebViewClient : WebViewClientCompat() {
        private var initialScaleChanged = false
        private var initialScale = 0f

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

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            super.onScaleChanged(view, oldScale, newScale)
            logEvent("onScaleChanged", "oldScale" to oldScale, "newScale" to newScale)

            trackInitialScale(oldScale)

            if (isAtInitialScale(newScale)) {
                callback { it.onZoomReset(newScale) }
            } else {
                callback { it.onZoomed(newScale) }
            }
        }

        private fun trackInitialScale(scale: Float) {
            if (!initialScaleChanged) {
                initialScaleChanged = true
                initialScale = scale
            }
        }

        private fun isAtInitialScale(scale: Float): Boolean {
            return initialScale == scale
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
                    true -> TurbolinksVisitOptions(action = TurbolinksVisitAction.REPLACE)
                    else -> TurbolinksVisitOptions(action = TurbolinksVisitAction.ADVANCE)
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
            val requestHandler = offlineRequestHandler ?: return null

            if (!request.method.equals("GET", ignoreCase = true) ||
                request.url.scheme?.startsWith("HTTP", ignoreCase = true) != true
            ) {
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

    /**
     * Create a new [TurbolinksSession].
     *
     * @constructor
     */
    companion object {
        fun getNew(sessionName: String, activity: Activity, webView: TurbolinksWebView): TurbolinksSession {
            return TurbolinksSession(sessionName, activity, webView)
        }
    }
}
