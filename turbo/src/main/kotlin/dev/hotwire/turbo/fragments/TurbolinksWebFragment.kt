package dev.hotwire.turbo.fragments

import android.os.Bundle
import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.delegates.TurbolinksWebFragmentDelegate
import dev.hotwire.turbo.session.TurbolinksSessionModalResult
import dev.hotwire.turbo.views.TurbolinksWebView

/**
 * The base class from which all web "standard" fragments (non-dialogs) in a Turbolinks driven app
 * should extend from.
 *
 * @constructor Create empty Turbolinks web fragment
 */
abstract class TurbolinksWebFragment : TurbolinksFragment(), TurbolinksWebFragmentCallback {
    private lateinit var delegate: TurbolinksWebFragmentDelegate

    /**
     * Instantiates a [TurbolinksWebFragmentDelegate].
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksWebFragmentDelegate(this, this)
    }

    /**
     * Passes this lifecycle call through to [TurbolinksWebFragmentDelegate.onActivityCreated].
     *
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    /**
     * Passes this lifecycle call through to [TurbolinksWebFragmentDelegate.onStart] if there is no
     * modal result to process.
     *
     */
    override fun onStart() {
        super.onStart()

        if (!sessionViewModel.modalResultExists) {
            delegate.onStart()
        }
    }

    /**
     * Passes this call through to [TurbolinksWebFragmentDelegate.onStartAfterModalResult]
     *
     * @param result
     */
    override fun onStartAfterModalResult(result: TurbolinksSessionModalResult) {
        super.onStartAfterModalResult(result)
        delegate.onStartAfterModalResult(result)
    }

    /**
     * Passes this call through to [TurbolinksWebFragmentDelegate.onStartAfterDialogCancel] if there
     * is no modal result to process.
     *
     */
    override fun onStartAfterDialogCancel() {
        super.onStartAfterDialogCancel()

        if (!sessionViewModel.modalResultExists) {
            delegate.onStartAfterDialogCancel()
        }
    }

    /**
     * Implementing classes can execute state cleanup by overriding this. Will always be called
     * before any navigation action takes place.
     *
     */
    override fun onBeforeNavigation() {
        // Allow subclasses to do state cleanup
    }

    // ----------------------------------------------------------------------------
    // TurbolinksWebFragmentCallback interface
    // ----------------------------------------------------------------------------


    override fun onWebViewAttached(webView: TurbolinksWebView) {}

    override fun onWebViewDetached(webView: TurbolinksWebView) {}

    override fun onColdBootPageStarted(location: String) {}

    override fun onColdBootPageCompleted(location: String) {}

    override fun onVisitStarted(location: String) {}

    override fun onVisitCompleted(location: String, completedOffline: Boolean) {}

    override fun onVisitErrorReceived(location: String, errorCode: Int) {
        delegate.showErrorView(errorCode)
    }

    override fun onVisitErrorReceivedWithCachedSnapshotAvailable(location: String, errorCode: Int) {
        // Allow app to display an indicator for (potentially) stale content
    }

    override fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String) {
        handler.cancel()
    }
}
