package com.basecamp.turbolinks.demo

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

@Suppress("unused")
class NativeBridge {
    private val webView: WebView? get() = listener?.onProvideWebView()
    var listener: BridgeListener? = null

    interface BridgeListener {
        fun onProvideWebView(): WebView?
        fun receive(event: BridgeEvent)
    }

    fun send(event: BridgeEvent) {
        val javascript = "AppBridge.send(${event.toJSON()}"
        webView?.evaluateJavascript(javascript) { result ->
            log("AppBridge.send() result: $result")
        }
    }

    @JavascriptInterface
    fun receive(event: String?) {
        log("receive() $event")
        BridgeEvent.fromJSON(event)?.let {
            listener?.receive(it)
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        Log.d("NativeBridge", message)
    }

    fun installScript() {
        webView?.evaluateJavascript(script()) {}
    }

    private fun script(): String {
        return webView?.context?.contentFromAsset("js/bridge.js") ?:
                throw IllegalStateException("Bridge script not available")
    }
}
