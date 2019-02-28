package com.basecamp.turbolinks.demo

import android.util.Log
import android.webkit.WebView
import com.basecamp.turbolinks.TurbolinksFragment
import com.basecamp.turbolinks.TurbolinksFragmentObserver

class BridgeFragmentObserver(fragment: TurbolinksFragment) : TurbolinksFragmentObserver(fragment) {
    private var bridge: NativeBridge? = null

    override fun onWebViewAttached() {
        super.onWebViewAttached()
        bridge = webView?.tag as NativeBridge?
        bridge?.listener = bridgeListener
    }

    override fun onWebViewDetached() {
        super.onWebViewDetached()
        bridge?.listener = null
        bridge = null
    }

    override fun onPageFinished(location: String) {
        super.onPageFinished(location)
        installBridge()
    }

    fun onReceiveBridgeEvent(event: BridgeEvent) {
        Log.d("Bridge", "Bridge event received: ${event.toJSON()}")
    }

    fun sendBridgeEvent(event: BridgeEvent) {
        bridge?.send(event)
    }

    private fun installBridge() {
        bridge?.installScript()
    }

    // -----------------------------------------------------------------------
    // BridgeListener interface
    // -----------------------------------------------------------------------

    private val bridgeListener = object : NativeBridge.BridgeListener {
        override fun receive(event: BridgeEvent) {
            onReceiveBridgeEvent(event)
        }

        override fun onProvideWebView(): WebView? {
            return webView
        }
    }
}
