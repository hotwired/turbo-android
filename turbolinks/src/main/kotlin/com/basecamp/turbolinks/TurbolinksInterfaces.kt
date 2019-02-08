package com.basecamp.turbolinks

interface TurbolinksCallback {
    fun onPageStarted(location: String)
    fun onPageFinished(location: String)
    fun shouldOverrideUrl(location: String)
    fun onReceivedError(errorCode: Int)
    fun pageInvalidated()
    fun requestFailedWithStatusCode(statusCode: Int)
    fun visitRendered()
    fun visitCompleted()
    fun visitLocationWithNewDestinationStarted(location: String)
    fun visitProposedToLocationWithAction(location: String, action: String)
}

internal interface TurbolinksScrollUpCallback {
    fun canChildScrollUp(): Boolean
}
