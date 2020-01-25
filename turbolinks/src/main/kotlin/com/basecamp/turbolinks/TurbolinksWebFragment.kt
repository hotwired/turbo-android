package com.basecamp.turbolinks

import android.os.Bundle
import android.webkit.WebView

abstract class TurbolinksWebFragment : TurbolinksNativeFragment(), TurbolinksWebFragmentCallback {
    private lateinit var delegate: TurbolinksWebFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksWebFragmentDelegate(this, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    // ----------------------------------------------------------------------------
    // TurbolinksWebFragmentCallback interface
    // ----------------------------------------------------------------------------

    override fun onUpdateView() {}

    override fun onWebViewAttached() {}

    override fun onWebViewDetached() {}

    override fun onColdBootPageStarted(location: String) {}

    override fun onColdBootPageCompleted(location: String) {}

    override fun onVisitStarted(location: String) {}

    override fun onVisitCompleted(location: String) {}

    override fun onVisitErrorReceived(location: String, errorCode: Int) {}
}
