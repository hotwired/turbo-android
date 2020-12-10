package dev.hotwire.turbo.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.webkit.HttpAuthHandler
import dev.hotwire.turbo.delegates.TurboWebFragmentDelegate
import dev.hotwire.turbo.views.TurboWebView

/**
 * The base class from which all bottom sheet web fragments in a Turbo driven app
 * should extend from.
 *
 * @constructor Create empty Turbo web bottom sheet dialog fragment
 */
@Suppress("unused")
abstract class TurboWebBottomSheetDialogFragment : TurboBottomSheetDialogFragment(),
    TurboWebFragmentCallback {
    private lateinit var delegate: TurboWebFragmentDelegate

    /**
     * Instantiates a [TurboWebFragmentDelegate].
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurboWebFragmentDelegate(this, this)
    }

    /**
     * Passes this lifecycle call through to [TurboWebFragmentDelegate.onActivityCreated].
     *
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    /**
     * Passes this lifecycle call through to [TurboWebFragmentDelegate.onStart] if there is no
     * modal result to process.
     *
     */
    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    /**
     * Passes this call through to [TurboWebFragmentDelegate.onDialogCancel].
     *
     * @param result
     */
    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
    }

    /**
     * Passes this call through to [TurboWebFragmentDelegate.onDialogDismiss].
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
    // TurboWebFragmentCallback interface
    // ----------------------------------------------------------------------------


    override fun onWebViewAttached(webView: TurboWebView) {}

    override fun onWebViewDetached(webView: TurboWebView) {}

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
