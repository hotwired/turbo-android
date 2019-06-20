package com.basecamp.turbolinks.demo

import android.util.Log
import android.webkit.WebView

class BridgeFragmentDelegate(val fragment: WebFragment) {
    private var bridge: NativeBridge? = null

    fun load() {
        bridge?.installScript()
    }

    fun onWebViewAttached() {
        bridge = fragment.webView?.tag as NativeBridge?
        bridge?.listener = bridgeListener
    }

    fun onWebViewDetached() {
        bridge?.listener = null
        bridge = null
    }

    fun onReceiveBridgeEvent(event: BridgeEvent) {
        Log.d("Bridge", "Bridge event received: ${event.toJSON()}")
    }

    fun sendBridgeEvent(event: BridgeEvent) {
        bridge?.send(event)
    }

    // -----------------------------------------------------------------------
    // BridgeListener interface
    // -----------------------------------------------------------------------

    private val bridgeListener = object : NativeBridge.BridgeListener {
        override fun receive(event: BridgeEvent) {
            onReceiveBridgeEvent(event)
        }

        override fun onProvideWebView(): WebView? {
            return fragment.webView
        }
    }
}
