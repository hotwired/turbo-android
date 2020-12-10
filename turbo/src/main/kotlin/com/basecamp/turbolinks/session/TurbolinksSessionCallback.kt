package com.basecamp.turbolinks.session

import android.webkit.HttpAuthHandler
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

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
    fun visitProposedToLocation(location: String, options: TurbolinksVisitOptions)
    fun isActive(): Boolean
}
