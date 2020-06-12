package com.basecamp.turbolinks

import android.view.View
import android.webkit.HttpAuthHandler

interface TurbolinksSessionCallback {
    fun onPageStarted(location: String)
    fun onPageFinished(location: String)
    fun onReceivedError(errorCode: Int)
    fun onRenderProcessGone()
    fun pageInvalidated()
    fun requestFailedWithStatusCode(statusCode: Int)
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
    fun visitRendered()
    fun visitCompleted(completedOffline: Boolean)
    fun visitLocationStarted(location: String)
    fun visitProposedToLocation(location: String, options: VisitOptions)
    fun isActive(): Boolean
    fun onNonMainFrameRequest(location: String) // TODO: New callback to give app an option to respond to non-main frame requests
}

interface TurbolinksActivity {
    var delegate: TurbolinksActivityDelegate
}

interface TurbolinksWebFragmentCallback {
    // View
    val turbolinksView: TurbolinksView?
    fun createErrorView(statusCode: Int): View
    fun createProgressView(location: String): View
    fun shouldEnablePullToRefresh(): Boolean

    // Events
    fun onUpdateView()
    fun onWebViewAttached()
    fun onWebViewDetached()
    fun onColdBootPageStarted(location: String)
    fun onColdBootPageCompleted(location: String)
    fun onVisitStarted(location: String)
    fun onVisitCompleted(location: String, completedOffline: Boolean)
    fun onVisitErrorReceived(location: String, errorCode: Int)
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
    fun onNonMainFrameRequest(location: String) // TODO: New callback to give app an option to respond to non-main frame requests
}
