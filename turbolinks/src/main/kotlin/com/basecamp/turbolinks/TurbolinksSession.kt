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
class TurbolinksSession private constructor(val activity: Activity, val webView: TurbolinksWebView) {
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
    internal var isWebViewAddedToNewParent: Boolean = false
    internal var javascriptInterfaces = HashMap<String, Any>()
    internal var previousTime: Long = 0
    internal var restoreWithCachedSnapshot: Boolean = false
    internal var restorationIdentifiers = SparseArray<String>()
    internal var visits = ArrayList<String>()
    internal val context: Context

    internal lateinit var location: String
    internal lateinit var tlCallback: TurbolinksCallback

    // User accessible
    val turbolinksSessionId: Int = Random().nextInt()
    var enableDebugLogging: Boolean = false
        set(value) {
            TurbolinksLog.enableDebugLogging = value
        }
    var enableScreenshots: Boolean = true
    var isColdBooting: Boolean = false
        internal set
    var isReady: Boolean = false
        internal set

    init {
        @Suppress("SENSELESS_COMPARISON") // Java could send null
        if (activity == null) throw IllegalArgumentException("Activity must not be null.")

        this.context = activity
        initializeWebView()
    }


    // Required

    fun callback(tlCallback: TurbolinksCallback): TurbolinksSession {
        this.tlCallback = tlCallback

        return this
    }

    fun visit(location: String) {
        this.location = location

        validateRequiredParams()
        visitLocation()
    }


    // Optional

    fun restoreWithCachedSnapshot(restoreWithCachedSnapshot: Boolean): TurbolinksSession {
        this.restoreWithCachedSnapshot = restoreWithCachedSnapshot
        return this
    }


    // Public

    fun reset() {
        coldBootVisitIdentifier = ""
        currentVisitIdentifier = ""
        restorationIdentifiers.clear()
        visits.clear()
        isLoadingBridge = false
        isReady = false
        isColdBooting = false
    }

    fun visitLocationWithAction(location: String, action: String) {
        val restorationIdentifier = restorationIdentifiers[destinationIdentifier()] ?: ""
        this.location = location
        val params = commaDelimitedJson(location.urlEncode(), action, restorationIdentifier)

        TurbolinksLog.d("visitLocationWithAction: [location: $location, action: $action, restorationIdentifier: $restorationIdentifier]")

        webView.executeJavascript("webView.visitLocationWithActionAndRestorationIdentifier($params)")
    }


    // Callbacks from Turbolinks Core

    @JavascriptInterface
    fun visitProposedToLocationWithAction(location: String, action: String) {
        TurbolinksLog.d("visitProposedToLocationWithAction: [location: $location, action: $action]")

        context.runOnUiThread { tlCallback.visitProposedToLocationWithAction(location, action) }
    }

    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun visitStarted(visitIdentifier: String, visitHasCachedSnapshot: Boolean, location: String, restorationIdentifier: String) {
        TurbolinksLog.d("visitStarted: [location: $location, visitIdentifier: $visitIdentifier, visitHasCachedSnapshot: $visitHasCachedSnapshot, restorationIdentifier: $restorationIdentifier]")

        restorationIdentifiers.put(destinationIdentifier(), restorationIdentifier)
        currentVisitIdentifier = visitIdentifier
        visits.add(location)

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

        restoreWithCachedSnapshot = false

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread { tlCallback.requestFailedWithStatusCode(statusCode) }
        }
    }

    @JavascriptInterface
    fun pageLoaded(restorationIdentifier: String) {
        TurbolinksLog.d("pageLoaded: [restorationIdentifier: $restorationIdentifier]")

        restorationIdentifiers.put(destinationIdentifier(), restorationIdentifier)
    }

    @JavascriptInterface
    fun visitRendered(visitIdentifier: String) {
        TurbolinksLog.d("visitRendered: [visitIdentifier: $visitIdentifier]")

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread {
                tlCallback.visitRendered()
            }
        }
    }

    @JavascriptInterface
    fun visitCompleted(visitIdentifier: String) {
        TurbolinksLog.d("visitCompleted: [visitIdentifier: $visitIdentifier]")

        visits.clear()
        restoreWithCachedSnapshot = false

        if (visitIdentifier == currentVisitIdentifier) {
            context.runOnUiThread {
                tlCallback.visitCompleted()
            }
        }
    }

    @JavascriptInterface
    fun pageInvalidated() {
        TurbolinksLog.d("pageInvalidated")

        reset()
        restoreWithCachedSnapshot = false

        context.runOnUiThread {
            tlCallback.pageInvalidated()
            visitLocation(reload = true)
        }
    }

    @JavascriptInterface
    fun turbolinksIsReady(isReady: Boolean) {
        this.isReady = isReady

        if (isReady) {
            isLoadingBridge = false
            isColdBooting = false

            // Pending visits were queued while cold booting -- visit the current location
            if (visits.size > 0) {
                TurbolinksLog.d("setTurbolinksIsReady pending visit: [location: $location]")

                visitLocationWithAction(location, action())
                visits.clear()
            } else {
                TurbolinksLog.d("setTurbolinksIsReady calling visitRendered")

                webView.executeJavascript("window.webView.afterNextRepaint(function() { TurbolinksSession.visitRendered('$coldBootVisitIdentifier') })")
                context.runOnUiThread { tlCallback.visitCompleted() }
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
            tlCallback.onReceivedError(-1)
        }
    }

    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(jsInterface: Any, name: String) {
        if (name == JS_RESERVED_INTERFACE_NAME) throw IllegalArgumentException("$JS_RESERVED_INTERFACE_NAME is a reserved Javascript Interface name.")

        if (javascriptInterfaces[name] == null) {
            javascriptInterfaces[name] = jsInterface
            webView.addJavascriptInterface(jsInterface, name)
        }
    }


    // Private

    private fun action() = if (restoreWithCachedSnapshot) ACTION_RESTORE else ACTION_ADVANCE

    private fun destinationIdentifier(): Int {
        return requireNotNull(tlCallback).identifier()
    }

    private fun validateRequiredParams() {
        requireNotNull(tlCallback) { "TurbolinksSession.callback(callback) must be called with a non-null object." }
        requireNotNull(location) { "TurbolinksSession.visit(location) location value must not be null." }
    }

    private fun visitLocation(reload: Boolean = false) {
        tlCallback.visitLocationStarted(location)

        if (isColdBooting) {
            visits.add(location)
        }

        if (isReady) {
            visitLocationWithAction(location, action())
        }

        if (!isReady && !isColdBooting) {
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
            tlCallback.onPageStarted(location)
            currentVisitIdentifier = location.hashCode().toString()
        }

        override fun onPageFinished(view: WebView, location: String) {
            TurbolinksLog.d("onPageFinished: [location: $location, progress: ${view.progress}]")

            if (view.progress < 100) return

            TurbolinksLog.d("onPageFinished: [location: $location]")
            coldBootVisitIdentifier = location.hashCode().toString()
            val expression = "window.webView == null"
            webView.evaluateJavascript(expression) { s ->
                if (s?.toBoolean() == true && !isLoadingBridge) {
                    isLoadingBridge = true
                    loadJavascriptBridge(context, webView)

                    TurbolinksLog.d("Bridge loaded")

                    tlCallback.onPageFinished(location)
                }
            }
        }

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView, location: String): Boolean {
            TurbolinksLog.d("Overriding load: [location: $location]")

            tlCallback.shouldOverrideUrl(location)

            if (!isReady || isColdBooting) return false

            val currentTime = Date().time
            if (currentTime - previousTime > 500) {
                TurbolinksLog.d("Overriding load: [location: $location]")

                previousTime = currentTime
                visitProposedToLocationWithAction(location, ACTION_ADVANCE)
            }

            return true
        }

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            TurbolinksLog.d("onReceivedError: [errorCode: $errorCode]")

            super.onReceivedError(view, errorCode, description, failingUrl)
            reset()

            tlCallback.onReceivedError(errorCode)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)

            if (request.isForMainFrame) {
                TurbolinksLog.d("onReceivedHttpError: [statusCode: ${errorResponse.statusCode}]")

                reset()
                tlCallback.onReceivedError(errorResponse.statusCode)
            }
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
