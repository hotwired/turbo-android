package com.basecamp.turbolinks.fragments

import android.view.View
import android.webkit.HttpAuthHandler
import com.basecamp.turbolinks.views.TurbolinksView
import com.basecamp.turbolinks.views.TurbolinksWebView

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
