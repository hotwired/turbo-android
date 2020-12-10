package dev.hotwire.turbo.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.delegates.TurbolinksWebFragmentDelegate
import dev.hotwire.turbo.views.TurbolinksWebView

/**
 * The base class from which all bottom sheet web fragments in a Turbolinks driven app
 * should extend from.
 *
 * @constructor Create empty Turbolinks web bottom sheet dialog fragment
 */
@Suppress("unused")
abstract class TurbolinksWebBottomSheetDialogFragment : TurbolinksBottomSheetDialogFragment(),
    TurbolinksWebFragmentCallback {
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
        delegate.onStart()
    }

    /**
     * Passes this call through to [TurbolinksWebFragmentDelegate.onDialogCancel].
     *
     * @param result
     */
    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
    }

    /**
     * Passes this call through to [TurbolinksWebFragmentDelegate.onDialogDismiss].
     *
     * @param result
     */
    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    /**
     * Implementing classes can execute state cleanup by overriding this. Will always be called
     * before any navigation action takes place.
     *
     */
    override fun onBeforeNavigation() {}

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
