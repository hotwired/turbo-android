package dev.hotwire.turbo.session

import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.visit.TurboVisitOptions

internal interface TurboSessionCallback {
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
    fun visitProposedToLocation(location: String, options: TurboVisitOptions)
    fun visitNavDestination(): TurboNavDestination
    fun formSubmissionStarted(location: String)
    fun formSubmissionFinished(location: String)
}
