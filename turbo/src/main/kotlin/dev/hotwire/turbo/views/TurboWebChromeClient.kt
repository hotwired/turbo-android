package dev.hotwire.turbo.views

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import dev.hotwire.turbo.session.TurboSession

open class TurboWebChromeClient(val session: TurboSession) : WebChromeClient() {
    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        return session.fileChooserDelegate.onShowFileChooser(
            filePathCallback = filePathCallback,
            params = fileChooserParams
        )
    }
}
