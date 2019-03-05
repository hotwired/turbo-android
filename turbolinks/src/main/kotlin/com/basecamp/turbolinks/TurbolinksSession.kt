package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import java.util.*
import kotlin.random.Random

@Suppress("unused")
class TurbolinksSession private constructor(val context: Context, val webView: TurbolinksWebView) {
    // Internal state management
    internal var coldBootVisitIdentifier = ""
    internal var previousTime: Long = 0
    internal var restorationIdentifiers = SparseArray<String>()
    internal var pendingVisits = ArrayList<String>()

    internal lateinit var currentVisit: TurbolinksVisit
    internal val destinationIdentifier: Int
        get() = currentVisit.destinationIdentifier
    internal val callback: TurbolinksSessionCallback
        get() = currentVisit.callback

    // User accessible
    val sessionId: Int = Random.nextInt(0, 99999)
    var enableScreenshots: Boolean = true
    var isColdBooting: Boolean = false
        internal set
    var isReady: Boolean = false
        internal set

    init {
        initializeWebView()
    }


    // Required

    fun visit(visit: TurbolinksVisit) {
        this.currentVisit = visit
        visitLocation(reload = visit.reload)
    }


    // Public

    fun reset() {
        currentVisit.identifier = ""
        coldBootVisitIdentifier = ""
        restorationIdentifiers.clear()
        pendingVisits.clear()
        isReady = false
        isColdBooting = false
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        TurbolinksLog.enableDebugLogging = enabled
    }


    // Callbacks from Turbolinks Core

    @JavascriptInterface
    fun visitProposedToLocationWithAction(location: String, action: String) {
        logEvent("visitProposedToLocationWithAction", "location" to location, "action" to action)
        context.runOnUiThread { callback.visitProposedToLocationWithAction(location, action) }
    }

    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String, restorationIdentifier: String) {
        logEvent("visitStarted", "location" to location,
                "visitIdentifier" to visitIdentifier,
                "visitHasCachedSnapshot" to visitHasCachedSnapshot,
                "restorationIdentifier" to restorationIdentifier)

        restorationIdentifiers.put(destinationIdentifier, restorationIdentifier)
        currentVisit.identifier = visitIdentifier
        pendingVisits.add(location)

        val params = commaDelimitedJson(visitIdentifier)
        webView.runJavascript("webView.changeHistoryForVisitWithIdentifier($params)")
        webView.runJavascript("webView.issueRequestForVisitWithIdentifier($params)")
        webView.runJavascript("webView.loadCachedSnapshotForVisitWithIdentifier($params)")
    }

    @JavascriptInterface
    fun visitRequestCompleted(visitIdentifier: String) {
        logEvent("visitRequestCompleted", "visitIdentifier" to visitIdentifier)

        if (visitIdentifier == currentVisit.identifier) {
            val params = commaDelimitedJson(visitIdentifier)
            webView.runJavascript("webView.loadResponseForVisitWithIdentifier($params)")
        }
    }

    @JavascriptInterface
    fun visitRequestFailedWithStatusCode(visitIdentifier: String, statusCode: Int) {
        logEvent("visitRequestFailedWithStatusCode",
                "visitIdentifier" to visitIdentifier,
                "statusCode" to statusCode)

        if (visitIdentifier == currentVisit.identifier) {
            context.runOnUiThread { callback.requestFailedWithStatusCode(statusCode) }
        }
    }

    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        logEvent("pageLoaded", "restorationIdentifier" to restorationIdentifier)
        restorationIdentifiers.put(destinationIdentifier, restorationIdentifier)
    }

    @JavascriptInterface
    fun visitRendered(visitIdentifier: String) {
        logEvent("visitRendered", "visitIdentifier" to visitIdentifier)

        if (visitIdentifier == currentVisit.identifier) {
            context.runOnUiThread {
                callback.visitRendered()
            }
        }
    }

    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String) {
        logEvent("visitCompleted", "visitIdentifier" to visitIdentifier)
        pendingVisits.clear()

        if (visitIdentifier == currentVisit.identifier) {
            context.runOnUiThread {
                callback.visitCompleted()
            }
        }
    }

    @JavascriptInterface
    fun pageInvalidated() {
        logEvent("pageInvalidated")

        context.runOnUiThread {
            callback.pageInvalidated()
            visitLocation(reload = true)
        }
    }

    @JavascriptInterface
    fun turbolinksIsReady(isReady: Boolean) {
        this.isReady = isReady
        val location = currentVisit.location

        if (isReady) {
            logEvent("turbolinksIsReady")
            isColdBooting = false

            // Pending visits were queued while cold booting -- visit the current location
            if (pendingVisits.size > 0) {
                logEvent("pending visit", "location" to location)
                visitLocationWithAction(currentVisit)
                pendingVisits.clear()
            } else {
                logEvent("visitRenderedForColdBoot", "coldBootVisitIdentifier" to coldBootVisitIdentifier)
                webView.runJavascript("webView.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
                context.runOnUiThread { callback.visitCompleted() }
            }
        } else {
            logEvent("TurbolinksSession is not ready. Resetting and passing error.")
            reset()
            visitRequestFailedWithStatusCode(currentVisit.identifier, 500)
        }
    }

    @JavascriptInterface
    fun turbolinksFailedToLoad() {
        context.runOnUiThread {
            logEvent("turbolinksFailedToLoad")
            reset()
            callback.onReceivedError(-1)
        }
    }

    // Private

    private fun visitLocationWithAction(visit: TurbolinksVisit) {
        val location = visit.location
        val action = visit.action
        val restorationIdentifier = when (action) {
            ACTION_RESTORE -> restorationIdentifiers[visit.destinationIdentifier] ?: ""
            ACTION_ADVANCE -> ""
            else -> ""
        }

        logEvent("visitLocationWithAction",
                "location" to location, "action" to action,
                "restorationIdentifier" to restorationIdentifier)

        val params = commaDelimitedJson(location.urlEncode(), action, restorationIdentifier)
        webView.runJavascript("webView.visitLocationWithActionAndRestorationIdentifier($params)")
    }

    private fun visitLocation(reload: Boolean = false) {
        val location = currentVisit.location
        callback.visitLocationStarted(location)

        if (reload) {
            reset()
        }

        if (isColdBooting) {
            pendingVisits.add(location)
            return
        }

        if (isReady) {
            visitLocationWithAction(currentVisit)
            return
        }

        logEvent("visit cold", "location" to location)
        isColdBooting = true

        // When a page is invalidated by Turbolinks, we need to reload the
        // same URL in the WebView. For a URL with an anchor, the WebView
        // sees a WebView.loadUrl() request as a same-page visit instead of
        // requesting a full page reload. To work around this, we call
        // WebView.reload(), which fully reloads the page for all URLs.
        when (reload) {
            true -> webView.reload()
            else -> webView.loadUrl(location)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView.apply {
            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addJavascriptInterface(this@TurbolinksSession, "TurbolinksSession")
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
        }
    }

    private fun installBridge(onBridgeInstalled: () -> Unit) {
        logEvent("installBridge")
        webView.evaluateJavascript(bridgeScript()) {
            onBridgeInstalled()
        }
    }

    private fun bridgeScript(): String {
        return context.contentFromAsset("js/turbolinks_bridge.js")
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply { add(0, "session" to sessionId) }
        val description = attributes.joinToString(prefix = "[", postfix = "]", separator = ", ") {
            "${it.first}: ${it.second}"
        }
        TurbolinksLog.d("$event: $description")
    }


    // Classes and objects

    inner class TurbolinksWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, location: String, favicon: Bitmap?) {
            logEvent("onPageStarted", "location" to location)
            callback.onPageStarted(location)
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

            logEvent("onPageFinished", "location" to location, "progress" to view.progress)
            coldBootVisitIdentifier = location.identifier()

            webView.evaluateJavascript("window.webView == null") { s ->
                if (s?.toBoolean() == true) {
                    installBridge {
                        callback.onPageFinished(location)
                    }
                }
            }
        }

        /**
         * Turbolinks will not call adapter.visitProposedToLocationWithAction in some cases,
         * like target=_blank or when the domain doesn't match. We still route those here.
         * This is only called when links within a webView are clicked and not during loadUrl.
         * So this is safely ignored for the first cold boot.
         * http://stackoverflow.com/a/6739042/3280911
         */
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val newLocation = request.url.toString()
            logEvent("shouldOverrideUrlLoading", "location" to newLocation)

            if (!isReady || isColdBooting) {
                return false
            }

            if (callback.shouldOverrideUrl(newLocation)) {
                return true
            }

            // Prevents firing twice in a row within a few milliseconds of each other, which
            // happens sometimes. So we check for a slight delay between requests, which is
            // plenty of time to allow for a user to click the same link again.
            val currentTime = Date().time
            if (currentTime - previousTime > 500) {
                previousTime = currentTime
                visitProposedToLocationWithAction(newLocation, ACTION_ADVANCE)
            }

            return true
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)

            if (request.isForMainFrame) {
                logEvent("onReceivedHttpError", "statusCode" to errorResponse.statusCode)
                reset()
                callback.onReceivedError(errorResponse.statusCode)
            }
        }

        private fun String.identifier(): String {
            return hashCode().toString()
        }
    }

    internal class DefaultTurbolinksWebView constructor(context: Context, attrs: AttributeSet? = null) :
            TurbolinksWebView(context, attrs)

    companion object {
        const val ACTION_ADVANCE = "advance"
        const val ACTION_RESTORE = "restore"
        const val ACTION_REPLACE = "replace"

        fun getNew(activity: Activity, webView: TurbolinksWebView = DefaultTurbolinksWebView(activity)): TurbolinksSession {
            return TurbolinksSession(activity, webView)
        }
    }
}
