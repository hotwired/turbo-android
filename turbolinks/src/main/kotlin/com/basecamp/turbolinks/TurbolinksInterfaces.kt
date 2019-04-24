package com.basecamp.turbolinks

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
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
    fun onProvideSession(fragment: Fragment): TurbolinksSession
    fun onProvideSessionRootLocation(): String?
    fun onProvideRouter(): TurbolinksRouter
    fun onProvideCurrentNavHostFragment(): NavHostFragment
    fun onRequestFinish()
    fun navigate(location: String, action: String, properties: PathProperties? = null): Boolean
    fun navigateUp(): Boolean
    fun navigateBack()
    fun clearBackStack()
}

interface TurbolinksFragmentCallback {
    fun onProvideTurbolinksView(): TurbolinksView?
    fun onProvideErrorPlaceholder(): ViewGroup?
    fun onProvideToolbar(): Toolbar?
    fun onSetupToolbar()
    fun createErrorView(statusCode: Int): View
    fun createProgressView(location: String): View
    fun shouldEnablePullToRefresh(): Boolean
    fun onWebViewAttached()
    fun onWebViewDetached()
    fun onColdBootPageStarted(location: String)
    fun onColdBootPageFinished(location: String)
}
