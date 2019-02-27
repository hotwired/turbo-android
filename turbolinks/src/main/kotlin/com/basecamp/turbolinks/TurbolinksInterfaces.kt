package com.basecamp.turbolinks

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

interface TurbolinksSessionCallback {
    fun identifier(): Int
    fun onPageStarted(location: String)
    fun onPageFinished(location: String)
    fun shouldOverrideUrl(location: String)
    fun onReceivedError(errorCode: Int)
    fun pageInvalidated()
    fun requestFailedWithStatusCode(statusCode: Int)
    fun visitRendered()
    fun visitCompleted()
    fun visitLocationStarted(location: String)
    fun visitProposedToLocationWithAction(location: String, action: String)
}

interface TurbolinksActivity {
    fun onProvideSession(fragment: Fragment): TurbolinksSession
    fun onProvideRouter(): TurbolinksRouter
    fun onProvideCurrentDestination(): Fragment
    fun onRequestFinish()
    fun navigate(location: String, action: String)
    fun navigateUp(): Boolean
    fun navigateBack()
    fun clearBackStack()
}

interface TurbolinksFragment {
    fun onProvideObserver(): TurbolinksFragmentObserver
    fun onProvideTurbolinksView(): TurbolinksView?
    fun onProvideErrorPlaceholder(): ViewGroup?
    fun onSetupToolbar()
    fun createErrorView(statusCode: Int): View
    fun createProgressView(location: String): View
    fun shouldEnablePullToRefresh(): Boolean
    fun onTitleChanged(title: String)
}
