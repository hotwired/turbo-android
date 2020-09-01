package com.basecamp.turbolinks.demo

import com.basecamp.turbolinks.TurbolinksSession


fun TurbolinksSession.applyWebViewDefaults(): TurbolinksSession {
    return this.apply {
        val bridge = NativeBridge()
        webView.addJavascriptInterface(bridge, "NativeBridge")
        webView.tag = bridge
    }
}
