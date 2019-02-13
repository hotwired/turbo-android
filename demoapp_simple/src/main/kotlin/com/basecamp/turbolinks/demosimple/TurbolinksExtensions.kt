package com.basecamp.turbolinks.demosimple

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.basecamp.turbolinks.TurbolinksSession

fun TurbolinksSession.applyWebViewDefaults(): TurbolinksSession {
    return this.apply {
        webView.configureClientDefaults()

        if (BuildConfig.DEBUG) {
            // Clear the WebView cache so assets aren't cached between
            // app launches during development on the server.
            WebView(webView.context).clearCache(true)
            WebView.setWebContentsDebuggingEnabled(true)
            enableDebugLogging = true
        }
    }
}

private fun WebView.configureClientDefaults() {
    webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d("JS Console:", consoleMessage?.message())
            return super.onConsoleMessage(consoleMessage)
        }
    }
}
