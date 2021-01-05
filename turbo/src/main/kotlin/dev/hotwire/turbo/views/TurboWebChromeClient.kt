package dev.hotwire.turbo.views

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import dev.hotwire.turbo.nav.TurboNavDestination

open class TurboWebChromeClient(val destination: TurboNavDestination) : WebChromeClient() {
    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        return destination.session.fileUploadDelegate.onShowFileChooser(
            filePathCallback = filePathCallback,
            params = fileChooserParams,
            destination = destination
        )
    }
}
