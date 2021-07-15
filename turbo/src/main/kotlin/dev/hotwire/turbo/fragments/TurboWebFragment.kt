package dev.hotwire.turbo.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import dev.hotwire.turbo.R
import dev.hotwire.turbo.delegates.TurboWebFragmentDelegate
import dev.hotwire.turbo.session.TurboSessionModalResult
import dev.hotwire.turbo.util.TURBO_REQUEST_CODE_FILES
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebChromeClient

/**
 * The base class from which all web "standard" fragments (non-dialogs) in a
 * Turbo-driven app should extend from.
 *
 * For native fragments, refer to [TurboFragment].
 */
abstract class TurboWebFragment : TurboFragment(), TurboWebFragmentCallback {
    private lateinit var webDelegate: TurboWebFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webDelegate = TurboWebFragmentDelegate(delegate, this, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.turbo_fragment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webDelegate.onViewCreated()
    }

    override fun onStart() {
        super.onStart()

        if (!delegate.sessionViewModel.modalResultExists) {
            webDelegate.onStart()
        }
    }

    /**
     * Called when the Fragment has been started again after receiving a
     * modal result. Will navigate if the result indicates it should.
     */
    override fun onStartAfterModalResult(result: TurboSessionModalResult) {
        super.onStartAfterModalResult(result)
        webDelegate.onStartAfterModalResult(result)
    }

    /**
     * Called when the Fragment has been started again after a dialog has
     * been dismissed/canceled and no result is passed back.
     */
    override fun onStartAfterDialogCancel() {
        super.onStartAfterDialogCancel()

        if (!delegate.sessionViewModel.modalResultExists) {
            webDelegate.onStartAfterDialogCancel()
        }
    }

    override fun refresh(displayProgress: Boolean) {
        webDelegate.refresh(displayProgress)
    }

    override fun activityResultLauncher(requestCode: Int): ActivityResultLauncher<Intent>? {
        return when (requestCode) {
            TURBO_REQUEST_CODE_FILES -> webDelegate.fileChooserResultLauncher
            else -> null
        }
    }

    // ----------------------------------------------------------------------------
    // TurboWebFragmentCallback interface
    // ----------------------------------------------------------------------------

    /**
     * Gets the TurboView instance in the Fragment's view
     * with resource ID R.id.turbo_view.
     */
    final override val turboView: TurboView?
        get() = view?.findViewById(R.id.turbo_view)

    @SuppressLint("InflateParams")
    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.turbo_progress, null)
    }

    @SuppressLint("InflateParams")
    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.turbo_error, null)
    }

    override fun createWebChromeClient(): TurboWebChromeClient {
        return TurboWebChromeClient(session)
    }

    override fun onVisitErrorReceived(location: String, errorCode: Int) {
        webDelegate.showErrorView(errorCode)
    }
}
