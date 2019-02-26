package com.basecamp.turbolinks

import androidx.fragment.app.Fragment

interface TurbolinksCallback {
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
    fun onProvideSession(fragment: TurbolinksFragment): TurbolinksSession
    fun onProvideRouter(): TurbolinksRouter
    fun onProvideCurrentDestination(): Fragment
    fun onRequestFinish()
    fun navigate(location: String, action: String)
    fun navigateUp(): Boolean
    fun navigateBack()
    fun clearBackStack()
}
