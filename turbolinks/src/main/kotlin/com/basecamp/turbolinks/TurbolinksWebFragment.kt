package com.basecamp.turbolinks

import android.os.Bundle
import android.webkit.WebView

abstract class TurbolinksWebFragment : TurbolinksFragment(), TurbolinksWebFragmentCallback {
    private lateinit var delegate: TurbolinksWebFragmentDelegate

    val webView: WebView? get() = delegate.webView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate = TurbolinksWebFragmentDelegate(this, this, navigator)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    // ----------------------------------------------------------------------------
    // TurbolinksWebFragmentCallback interface
    // ----------------------------------------------------------------------------

    override fun onUpdateView() {
        initToolbar()
    }

    override fun onWebViewAttached() {}

    override fun onWebViewDetached() {}

    override fun onColdBootPageStarted(location: String) {}

    override fun onColdBootPageCompleted(location: String) {}

    override fun onVisitStarted(location: String) {}

    override fun onVisitCompleted(location: String) {}
}
