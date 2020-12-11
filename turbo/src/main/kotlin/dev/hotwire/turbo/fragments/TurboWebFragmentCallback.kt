package dev.hotwire.turbo.fragments

import android.view.View
import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebView

/**
 * Callback interface to be implemented by a [TurboWebFragment], [TurboWebBottomSheetDialogFragment],
 * or subclass. Typically called into by [TurboWebFragmentDelegate].
 *
 * @constructor Create empty Turbo web fragment callback
 */
interface TurboWebFragmentCallback {
    // View
    val turboView: TurboView?

    /**
     * Should inflate and return a new view to serve as an error view.
     *
     * @param statusCode
     * @return
     */
    fun createErrorView(statusCode: Int): View

    /**
     * Should inflate and return a new view to serve as a progress view.
     *
     * @param location
     * @return
     */
    fun createProgressView(location: String): View

    // Events
    /**
     * Called when the WebView has been attached to its new parent.
     *
     */
    fun onWebViewAttached(webView: TurboWebView)

    /**
     * Called when the WebView has been detached from its previous parent.
     *
     */
    fun onWebViewDetached(webView: TurboWebView)

    /**
     * Called when Turbo begins a cold boot (fresh resources).
     *
     * @param location
     */
    fun onColdBootPageStarted(location: String)

    /**
     * Called when Turbo completes a cold boot (fresh resources).
     *
     * @param location
     */
    fun onColdBootPageCompleted(location: String)

    /**
     * Called when a Turbo visit has started.
     *
     * @param location
     */
    fun onVisitStarted(location: String)

    /**
     * Called when a Turbo visit has completed.
     *
     * @param location
     * @param completedOffline
     */
    fun onVisitCompleted(location: String, completedOffline: Boolean)

    /**
     * Called when a Turbo visit resulted in an error.
     *
     * @param location
     * @param errorCode
     */
    fun onVisitErrorReceived(location: String, errorCode: Int)

    /**
     * Called when the Turbo visit resulted in an error, but has a cached snapshot that could
     * be displayed instead.
     *
     * @param location
     * @param errorCode
     */
    fun onVisitErrorReceivedWithCachedSnapshotAvailable(location: String, errorCode: Int)

    /**
     * Called when the WebView has received an HTTP authentication request.
     *
     * @param handler
     * @param host
     * @param realm
     */
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
}