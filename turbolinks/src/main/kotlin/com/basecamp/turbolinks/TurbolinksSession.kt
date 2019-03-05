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
import java.io.IOException
import java.util.*

@Suppress("unused")
class TurbolinksSession private constructor(val context: Context, val webView: TurbolinksWebView) {
    internal val JS_RESERVED_INTERFACE_NAME = "TurbolinksSession"
    internal val JS_BRIDGE_LOADER = "(function(){" +
            "var parent = document.getElementsByTagName('head').item(0);" +
            "var script = document.createElement('script');" +
            "script.type = 'text/javascript';" +
            "script.innerHTML = window.atob('%s');" +
            "parent.appendChild(script);" +
            "return true;})()"

    // Internal state management
    internal var coldBootVisitIdentifier = ""
    internal var currentVisitIdentifier: String = ""
    internal var isLoadingBridge: Boolean = false
    internal var previousTime: Long = 0
    internal var restorationIdentifiers = SparseArray<String>()
    internal var pendingVisits = ArrayList<String>()

    internal lateinit var currentVisit: TurbolinksVisit
    internal val destinationIdentifier: Int
        get() = currentVisit.destinationIdentifier
    internal val callback: TurbolinksSessionCallback
        get() = currentVisit.callback

    // User accessible
    val sessionId: Int = Random().nextInt()
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
        coldBootVisitIdentifier = ""
        currentVisitIdentifier = ""
        restorationIdentifiers.clear()
        pendingVisits.clear()
        isLoadingBridge = false
        isReady = false
        isColdBooting = false
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        TurbolinksLog.enableDebugLogging = enabled
    }


    // Callbacks from Turbolinks Core

    @JavascriptInterface
    fun visitProposedToLocationWithAction(location: String, action: String) {
        TurbolinksLog.d("visitProposedToLocationWithAction: [location: $location, action: $action]")

        context.runOnUiThread { callback.visitProposedToLocationWithAction(location, action) }
    }

    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String, restorationIdentifier: String) {
        TurbolinksLog.d("visitStarted: [location: $location, visitIdentifier: $visitIdentifier, visitHasCachedSnapshot: $visitHasCachedSnapshot, restorationIdentifier: $restorationIdentifier]")

        restorationIdentifiers.put(destinationIdentifier, restorationIdentifier)
        currentVisitIdentifier = visitIdentifier
        pendingVisits.add(location)

        val params = commaDelimitedJson(visitIdentifier)
        webView.executeJavascript("webView.changeHistoryForVisitWithIdentifier($params)")
        webView.executeJavascript("webView.issueRequestForVisitWithIdentifier($params)")
        webView.executeJavascript("webView.loadCachedSnapshotForVisitWithIdentifier($params)")
    }

    @JavascriptInterface
    fun visitRequestCompleted(visitIdentifier: String) {
        TurbolinksLog.d("visitRequestCompleted: [visitIdentifier: $visitIdentifier]")

        if (visitIdentifier == currentVisitIdentifier) {
            val params = commaDelimitedJson(visitIdentifier)
            webView.executeJavascript("webView.loadResponseForVisitWithIdentifier($params)")
        }
    }

    @JavascriptInterface
    fun visitRequestFailedWithStatusCode(visitIdentifier: String, statusCode: Int) {
        TurbolinksLog.d("visitRequestFailedWithStatusCode: [visitIdentifier: $visitIdentifier], statusCode: $statusCode")

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread { callback.requestFailedWithStatusCode(statusCode) }
        }
    }

    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        TurbolinksLog.d("pageLoaded: [restorationIdentifier: $restorationIdentifier]")

        restorationIdentifiers.put(destinationIdentifier, restorationIdentifier)
    }

    @JavascriptInterface
    fun visitRendered(visitIdentifier: String) {
        TurbolinksLog.d("visitRendered: [visitIdentifier: $visitIdentifier]")

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread {
                callback.visitRendered()
            }
        }
    }

    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String) {
        TurbolinksLog.d("visitCompleted: [visitIdentifier: $visitIdentifier]")

        pendingVisits.clear()

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread {
                callback.visitCompleted()
            }
        }
    }

    @JavascriptInterface
    fun pageInvalidated() {
        TurbolinksLog.d("pageInvalidated")

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
            isLoadingBridge = false
            isColdBooting = false

            // Pending visits were queued while cold booting -- visit the current location
            if (pendingVisits.size > 0) {
                TurbolinksLog.d("setTurbolinksIsReady pending visit: [location: $location]")

                visitLocationWithAction(currentVisit)
                pendingVisits.clear()
            } else {
                TurbolinksLog.d("setTurbolinksIsReady calling visitRendered")

                webView.executeJavascript("window.webView.afterNextRepaint(function() { TurbolinksSession.visitRendered('$coldBootVisitIdentifier') })")
                context.runOnUiThread { callback.visitCompleted() }
            }
        } else {
            TurbolinksLog.d("TurbolinksSession is not ready. Resetting and throw error.")

            reset()
            visitRequestFailedWithStatusCode(currentVisitIdentifier, 500)
        }
    }

    @JavascriptInterface
    fun turbolinksFailedToLoad() {
        context.runOnUiThread {
            TurbolinksLog.d("turbolinksFailedToLoad")

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

        TurbolinksLog.d("visitLocationWithAction: [location: $location, action: $action, restorationIdentifier: $restorationIdentifier]")

        val params = commaDelimitedJson(location.urlEncode(), action, restorationIdentifier)
        webView.executeJavascript("webView.visitLocationWithActionAndRestorationIdentifier($params)")
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

        TurbolinksLog.d("visit cold: [location: $location]")
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
                databaseEnabled = true
            }

            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addJavascriptInterface(this@TurbolinksSession, JS_RESERVED_INTERFACE_NAME)
            webChromeClient = WebChromeClient()
            webViewClient = TurbolinksWebViewClient()
        }
    }

    private fun loadJavascriptBridge(context: Context, webView: WebView) {
        try {
            val jsFunction = JS_BRIDGE_LOADER.format(context.contentFromAsset("js/turbolinks_bridge.js"))
            TurbolinksLog.d("loadJavascriptBridge")
            webView.executeJavascript(jsFunction)
        } catch (e: IOException) {
            TurbolinksLog.e("Failed to load bridge: $e")
        }
    }


    // Classes and objects

    inner class TurbolinksWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, location: String, favicon: Bitmap?) {
            TurbolinksLog.d("onPageStarted: [location: $location]")
            callback.onPageStarted(location)
            coldBootVisitIdentifier = ""
            currentVisitIdentifier = location.identifier()
        }

        override fun onPageFinished(view: WebView, location: String) {
            TurbolinksLog.d("onPageFinished: [location: $location, progress: ${view.progress}]")

            if (coldBootVisitIdentifier == location.identifier()) {
                // If we got here, onPageFinished() has already been called for
                // this location so bail. It's common for onPageFinished()
                // to be called multiple times when the document has initially
                // loaded and then when resources like images finish loading.
                return
            }

            TurbolinksLog.d("onPageFinished: [location: $location]")
            coldBootVisitIdentifier = location.identifier()
            val expression = "window.webView == null"
            webView.evaluateJavascript(expression) { s ->
                if (s?.toBoolean() == true && !isLoadingBridge) {
                    isLoadingBridge = true
                    loadJavascriptBridge(context, webView)

                    TurbolinksLog.d("Bridge loaded")

                    callback.onPageFinished(location)
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
            TurbolinksLog.d("Overriding load: [location: $newLocation]")

            callback.shouldOverrideUrl(newLocation)

            if (!isReady || isColdBooting) return false

            // Prevents firing twice in a row within a few milliseconds of each other, which
            // happens sometimes. So we check for a slight delay between requests, which is
            // plenty of time to allow for a user to click the same link again.
            val currentTime = Date().time
            if (currentTime - previousTime > 500) {
                TurbolinksLog.d("Overriding load: [location: $newLocation]")

                previousTime = currentTime
                visitProposedToLocationWithAction(newLocation, ACTION_ADVANCE)
            }

            return true
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)

            if (request.isForMainFrame) {
                TurbolinksLog.d("onReceivedHttpError: [statusCode: ${errorResponse.statusCode}]")

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
