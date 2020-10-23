package com.basecamp.turbolinks

import android.view.View
import android.webkit.HttpAuthHandler

interface TurbolinksSessionCallback {
    fun onPageStarted(location: String)
    fun onPageFinished(location: String)
    fun onReceivedError(errorCode: Int)
    fun onRenderProcessGone()
    fun onZoomed(newScale: Float)
    fun onZoomReset(newScale: Float)
    fun pageInvalidated()
    fun requestFailedWithStatusCode(visitHasCachedSnapshot: Boolean, statusCode: Int)
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
    fun visitRendered()
    fun visitCompleted(completedOffline: Boolean)
    fun visitLocationStarted(location: String)
    fun visitProposedToLocation(location: String, options: VisitOptions)
    fun isActive(): Boolean
}

interface TurbolinksActivity {
    var delegate: TurbolinksActivityDelegate
}

interface TurbolinksWebFragmentCallback {
    // View
    val turbolinksView: TurbolinksView?
    fun createErrorView(statusCode: Int): View
    fun createProgressView(location: String): View

    // Events
    fun onUpdateView()
    fun onWebViewAttached(webView: TurbolinksWebView)
    fun onWebViewDetached(webView: TurbolinksWebView)
    fun onColdBootPageStarted(location: String)
    fun onColdBootPageCompleted(location: String)
    fun onVisitStarted(location: String)
    fun onVisitCompleted(location: String, completedOffline: Boolean)
    fun onVisitErrorReceived(location: String, errorCode: Int)
    fun onVisitErrorReceivedWithCachedSnapshotAvailable(location: String, errorCode: Int)
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
}
