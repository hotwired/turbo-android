package com.basecamp.turbolinks

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

interface TurbolinksSessionCallback {
    fun onPageStarted(location: String)
    fun onPageFinished(location: String)
    fun onReceivedError(errorCode: Int)
    fun pageInvalidated()
    fun requestFailedWithStatusCode(statusCode: Int)
    fun visitRendered()
    fun visitCompleted()
    fun visitLocationStarted(location: String)
    fun visitProposedToLocation(location: String, action: String, properties: PathProperties)
}

interface TurbolinksActivity {
    fun onProvideDelegate(): TurbolinksActivityDelegate
}

interface TurbolinksWebFragmentCallback {
    // View
    fun onProvideTurbolinksView(): TurbolinksView?
    fun onProvideErrorPlaceholder(): ViewGroup?
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
    fun onVisitCompleted(location: String)
}
