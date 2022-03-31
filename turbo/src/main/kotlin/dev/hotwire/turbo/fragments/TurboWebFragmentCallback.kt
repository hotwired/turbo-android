package dev.hotwire.turbo.fragments

import android.view.View
import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebChromeClient
import dev.hotwire.turbo.views.TurboWebView

/**
 * Callback interface to be implemented by a [TurboWebFragment],
 * [TurboWebBottomSheetDialogFragment], or subclass.
 */
interface TurboWebFragmentCallback {
    /**
     * The TurboView instance located in the Fragment's view.
     */
    val turboView: TurboView?

    /**
     * Inflate and return a new view to serve as an error view.
     */
    fun createErrorView(statusCode: Int): View

    /**
     * Inflate and return a new view to serve as a progress view.
     */
    fun createProgressView(location: String): View

    /**
     * Create and return a new web chrome client instance.
     */
    fun createWebChromeClient(): TurboWebChromeClient

    /**
     * Called when the WebView has been attached to the current destination.
     */
    fun onWebViewAttached(webView: TurboWebView) {}

    /**
     * Called when the WebView has been detached from the current destination.
     */
    fun onWebViewDetached(webView: TurboWebView) {}

    /**
     * Called when Turbo begins a WebView cold boot (fresh resources).
     */
    fun onColdBootPageStarted(location: String) {}

    /**
     * Called when Turbo completes a WebView cold boot (fresh resources).
     */
    fun onColdBootPageCompleted(location: String) {}

    /**
     * Called when a Turbo visit has started.
     */
    fun onVisitStarted(location: String) {}

    /**
     * Called when a Turbo visit has rendered (from a cached snapshot or
     * from a fresh network request). This may be called multiple times
     * during a normal visit lifecycle.
     */
    fun onVisitRendered(location: String) {}

    /**
     * Called when a Turbo visit has completed.
     */
    fun onVisitCompleted(location: String, completedOffline: Boolean) {}

    /**
     * Called when a Turbo visit resulted in an error.
     */
    fun onVisitErrorReceived(location: String, errorCode: Int) {}

    /**
     * Called when a Turbo form submission has started.
     */
    fun onFormSubmissionStarted(location: String) {}

    /**
     * Called when a Turbo form submission has finished.
     */
    fun onFormSubmissionFinished(location: String) {}

    /**
     * Called when the Turbo visit resulted in an error, but a cached
     * snapshot is being displayed, which may be stale.
     */
    fun onVisitErrorReceivedWithCachedSnapshotAvailable(location: String, errorCode: Int) {}

    /**
     * Called when the WebView has received an HTTP authentication request.
     */
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String) {
        handler.cancel()
    }
}
