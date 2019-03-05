package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup.LayoutParams
import android.webkit.*
import android.widget.FrameLayout
import java.util.*
import kotlin.random.Random

@Suppress("unused")
class TurbolinksSession private constructor(val context: Context, val webView: TurbolinksWebView) {
    // Internal state management
    internal lateinit var currentVisit: TurbolinksVisit
    internal var coldBootVisitIdentifier = ""
    internal var previousTime: Long = 0
    internal var restorationIdentifiers = SparseArray<String>()
    internal var pendingVisits = ArrayList<String>()
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


    // Public

    fun visit(visit: TurbolinksVisit) {
        this.currentVisit = visit
        callback.visitLocationStarted(visit.location)

        if (visit.reload) {
            reset()
        }

        if (isColdBooting) {
            pendingVisits.add(visit.location)
            return
        }

        when (isReady) {
            true -> visitLocation(currentVisit)
            else -> visitLocationAsColdBoot(currentVisit)
        }
    }

    fun reset() {
        logEvent("reset")
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
        context.runOnUiThread {
            callback.visitProposedToLocationWithAction(location, action)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String, restorationIdentifier: String) {
        logEvent("visitStarted", "location" to location,
                "visitIdentifier" to visitIdentifier,
                "visitHasCachedSnapshot" to visitHasCachedSnapshot,
                "restorationIdentifier" to restorationIdentifier)

        restorationIdentifiers.put(currentVisit.destinationIdentifier, restorationIdentifier)
        currentVisit.identifier = visitIdentifier
        pendingVisits.add(location)

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
            context.runOnUiThread {
                callback.requestFailedWithStatusCode(statusCode)
            }
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

        // Check if pending visits were queued while cold booting.
        // If so, visit the most recent current location.
        when (pendingVisits.size > 0) {
            true -> visitPendingLocation(currentVisit)
            else -> renderVisitForColdBoot()
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

    private fun visitLocation(visit: TurbolinksVisit) {
        val restorationIdentifier = when (visit.action) {
            ACTION_RESTORE -> restorationIdentifiers[visit.destinationIdentifier] ?: ""
            ACTION_ADVANCE -> ""
            else -> ""
        }

        logEvent("visitLocation",
                "location" to visit.location,
                "action" to visit.action,
                "restorationIdentifier" to restorationIdentifier)

        val params = commaDelimitedJson(visit.location.urlEncode(), visit.action, restorationIdentifier)
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
        pendingVisits.clear()
    }

    private fun renderVisitForColdBoot() {
        logEvent("renderVisitForColdBoot", "coldBootVisitIdentifier" to coldBootVisitIdentifier)
        webView.runJavascript("webView.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
        context.runOnUiThread {
            callback.visitCompleted()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView.apply {
            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addJavascriptInterface(this@TurbolinksSession, "TurbolinksSession")
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
        }
    }

    private fun installBridge(onBridgeInstalled: () -> Unit) {
        logEvent("installBridge")

        val script = context.contentFromAsset("js/turbolinks_bridge.js")
        webView.evaluateJavascript(script) {
            onBridgeInstalled()
        }
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
