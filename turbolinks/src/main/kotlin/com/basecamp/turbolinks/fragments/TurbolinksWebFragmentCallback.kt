package com.basecamp.turbolinks.fragments

import android.view.View
import android.webkit.HttpAuthHandler
import com.basecamp.turbolinks.views.TurbolinksView

/**
 * Callback interface to be implemented by a [TurbolinksWebFragment], [TurbolinksWebBottomSheetDialogFragment],
 * or subclass. Typically called into by [TurbolinksWebFragmentDelegate].
 *
 * @constructor Create empty Turbolinks web fragment callback
 */
interface TurbolinksWebFragmentCallback {
    // View
    val turbolinksView: TurbolinksView?

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

    /**
     * On update view
     *
     */
    fun onUpdateView()

    /**
     * Called when the WebView has been attached to its new parent.
     *
     */
    fun onWebViewAttached()

    /**
     * Called when the WebView has been detached from its previous parent.
     *
     */
    fun onWebViewDetached()

    /**
     * Called when Turbolinks begins a cold boot (fresh resources).
     *
     * @param location
     */
    fun onColdBootPageStarted(location: String)

    /**
     * Called when Turbolinks completes a cold boot (fresh resources).
     *
     * @param location
     */
    fun onColdBootPageCompleted(location: String)

    /**
     * Called when a Turbolinks visit has started.
     *
     * @param location
     */
    fun onVisitStarted(location: String)

    /**
     * Called when a Turbolinks visit has completed.
     *
     * @param location
     * @param completedOffline
     */
    fun onVisitCompleted(location: String, completedOffline: Boolean)

    /**
     * Called when a Turbolinks visit resulted in an error.
     *
     * @param location
     * @param errorCode
     */
    fun onVisitErrorReceived(location: String, errorCode: Int)

    /**
     * Called when the Turbolinks visit resulted in an error, but has a cached snapshot that could
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
