package dev.hotwire.turbo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.HttpAuthHandler
import com.google.android.material.textview.MaterialTextView
import dev.hotwire.turbo.R
import dev.hotwire.turbo.delegates.TurboWebFragmentDelegate
import dev.hotwire.turbo.session.TurboSessionModalResult
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebView
import kotlinx.android.synthetic.main.turbo_error.*

/**
 * The base class from which all web "standard" fragments (non-dialogs) in a Turbo driven app
 * should extend from.
 *
 * @constructor Create empty Turbo web fragment
 */
abstract class TurboWebFragment : TurboFragment(), TurboWebFragmentCallback {
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

        if (!sessionViewModel.modalResultExists) {
            delegate.onStart()
        }
    }

    /**
     * Passes this call through to [TurboWebFragmentDelegate.onStartAfterModalResult]
     *
     * @param result
     */
    override fun onStartAfterModalResult(result: TurboSessionModalResult) {
        super.onStartAfterModalResult(result)
        delegate.onStartAfterModalResult(result)
    }

    /**
     * Passes this call through to [TurboWebFragmentDelegate.onStartAfterDialogCancel] if there
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
    // TurboWebFragmentCallback interface
    // ----------------------------------------------------------------------------
    override val turboView: TurboView?
        get() = view?.findViewById(R.id.turbo_view)

    @SuppressLint("InflateParams")
    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.turbo_progress, null)
    }

    @SuppressLint("InflateParams")
    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.turbo_error, null).apply {
            val message = context.getString(R.string.error_message)
            findViewById<MaterialTextView>(R.id.turbo_error_message).text = message
        }
    }

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
