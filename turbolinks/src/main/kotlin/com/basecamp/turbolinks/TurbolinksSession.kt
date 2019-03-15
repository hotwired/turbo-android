package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup.LayoutParams
import android.webkit.*
import android.widget.FrameLayout
import java.util.*

@Suppress("unused")
class TurbolinksSession private constructor(val context: Context, val webView: TurbolinksWebView) {
    internal lateinit var currentVisit: TurbolinksVisit
    internal var coldBootVisitIdentifier = ""
    internal var previousOverrideUrlTime = 0L
    internal var visitPending = false
    internal var restorationIdentifiers = SparseArray<String>()

    // User accessible

    val sessionId = generateSessionId()
    var enableScreenshots = true
    var isColdBooting = false
        internal set
    var isReady = false
        internal set

    init {
        initializeWebView()
    }


    // Public

    fun reset() {
        logEvent("reset")
        currentVisit.identifier = ""
        coldBootVisitIdentifier = ""
        restorationIdentifiers.clear()
        visitPending = false
        isReady = false
        isColdBooting = false
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
    fun visitProposedToLocationWithAction(location: String, action: String) {
        logEvent("visitProposedToLocationWithAction", "location" to location, "action" to action)
        callback { it.visitProposedToLocationWithAction(location, action) }
    }

    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String, restorationIdentifier: String) {
        logEvent("visitStarted", "location" to location,
                "visitIdentifier" to visitIdentifier,
                "visitHasCachedSnapshot" to visitHasCachedSnapshot,
                "restorationIdentifier" to restorationIdentifier)

        restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
        currentVisit.identifier = visitIdentifier

        val params = commaDelimitedJson(visitIdentifier)
        val script = """
            webView.changeHistoryForVisitWithIdentifier($params);
            webView.issueRequestForVisitWithIdentifier($params);
            webView.loadCachedSnapshotForVisitWithIdentifier($params);
        """.trimIndent()

        webView.runJavascript(script)
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
            callback { it.requestFailedWithStatusCode(statusCode) }
        }
    }

    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        logEvent("pageLoaded", "restorationIdentifier" to restorationIdentifier)
        restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
    }

    @JavascriptInterface
    fun visitRendered(visitIdentifier: String) {
        logEvent("visitRendered", "visitIdentifier" to visitIdentifier)

        if (visitIdentifier == currentVisit.identifier) {
            callback { it.visitRendered() }
        }
    }

    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String) {
        logEvent("visitCompleted", "visitIdentifier" to visitIdentifier)

        if (visitIdentifier == currentVisit.identifier) {
            callback { it.visitCompleted() }
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
        val restorationIdentifier = when (visit.action) {
            ACTION_RESTORE -> restorationIdentifiers[visit.destinationIdentifier] ?: ""
            ACTION_ADVANCE -> ""
            else -> ""
        }

        val action = when (restorationIdentifier) {
            "" -> ACTION_ADVANCE
            else -> visit.action
        }

        logEvent("visitLocation",
                "location" to visit.location,
                "action" to action,
                "restorationIdentifier" to restorationIdentifier)

        val params = commaDelimitedJson(visit.location.urlEncode(), action, restorationIdentifier)
        webView.runJavascript("webView.visitLocationWithActionAndRestorationIdentifier($params)")
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
        webView.runJavascript("webView.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
        callback { it.visitCompleted() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addJavascriptInterface(this@TurbolinksSession, "TurbolinksSession")
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
        }
    }

    private fun installBridge(onBridgeInstalled: () -> Unit) {
        logEvent("installBridge")

        val script = "window.webView == null"
        val bridge = context.contentFromAsset("js/turbolinks_bridge.js")

        webView.evaluateJavascript(script) { s ->
            if (s?.toBoolean() == true) {
                webView.evaluateJavascript(bridge) {
                    onBridgeInstalled()
                }
            }
        }
    }

    private fun callback(action: (TurbolinksSessionCallback) -> Unit) {
        context.runOnUiThread {
            action(currentVisit.callback)
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply { add(0, "session" to sessionId) }
        val description = attributes.joinToString(prefix = "[", postfix = "]", separator = ", ") {
            "${it.first}: ${it.second}"
        }
        TurbolinksLog.d("$event ".padEnd(35, '.') + " $description")
    }


    // Classes and objects

    private inner class TurbolinksWebViewClient : WebViewClient() {
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

            logEvent("onPageFinished", "location" to location, "progress" to view.progress)
            coldBootVisitIdentifier = location.identifier()
            installBridge {
                callback { it.onPageFinished(location) }
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
            val location = request.url.toString()
            logEvent("shouldOverrideUrlLoading", "location" to location)

            if (!isReady || isColdBooting) {
                return false
            }

            if (shouldProposeThrottledVisit()) {
                visitProposedToLocationWithAction(location, ACTION_ADVANCE)
            }

            return true
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)

            if (request.isForMainFrame) {
                logEvent("onReceivedHttpError", "statusCode" to errorResponse.statusCode)
                reset()
                callback { it.onReceivedError(errorResponse.statusCode) }
            }
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

    internal class DefaultTurbolinksWebView constructor(context: Context, attrs: AttributeSet? = null) :
            TurbolinksWebView(context, attrs)

    companion object {
        const val ACTION_ADVANCE = "advance"
        const val ACTION_RESTORE = "restore"
        const val ACTION_REPLACE = "replace"

        private var sessionCount = 0

        private fun generateSessionId(): Int {
            return ++sessionCount
        }

        fun getNew(activity: Activity, webView: TurbolinksWebView = DefaultTurbolinksWebView(activity)): TurbolinksSession {
            return TurbolinksSession(activity, webView)
        }
    }
}
