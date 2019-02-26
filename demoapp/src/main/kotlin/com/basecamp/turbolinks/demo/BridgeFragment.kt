package com.basecamp.turbolinks.demo

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.basecamp.turbolinks.TurbolinksFragment
import com.basecamp.turbolinks.TurbolinksFragmentDelegate

class BridgeFragmentDelegate(fragment: TurbolinksFragment) : TurbolinksFragmentDelegate(fragment) {
    private var bridge: NativeBridge? = null

    override fun attach(context: Context) {
        super.attach(context)
        // TODO move to onWebViewAttached()
        bridge = webView?.tag as NativeBridge?
        bridge?.listener = bridgeListener
    }

    override fun detach() {
        super.detach()
        // TODO move to onWebViewDetached()
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
