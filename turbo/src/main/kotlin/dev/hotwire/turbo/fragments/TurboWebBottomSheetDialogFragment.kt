package dev.hotwire.turbo.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import com.google.android.material.textview.MaterialTextView
import dev.hotwire.turbo.R
import dev.hotwire.turbo.delegates.TurboWebFragmentDelegate
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebView

/**
 * The base class from which all bottom sheet web fragments in a
 * Turbo-driven app should extend from.
 *
 * For native bottom sheet fragments, refer to [TurboBottomSheetDialogFragment].
 */
@Suppress("unused")
abstract class TurboWebBottomSheetDialogFragment : TurboBottomSheetDialogFragment(),
    TurboWebFragmentCallback {
    private lateinit var webDelegate: TurboWebFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webDelegate = TurboWebFragmentDelegate(delegate, this, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.turbo_fragment_web_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        webDelegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()
        webDelegate.onStart()
    }

    override fun onCancel(dialog: DialogInterface) {
        webDelegate.onDialogCancel()
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        webDelegate.onDialogDismiss()
        super.onDismiss(dialog)
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
        return layoutInflater.inflate(R.layout.turbo_progress_bottom_sheet, null)
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
        webDelegate.showErrorView(errorCode)
    }

    override fun onVisitErrorReceivedWithCachedSnapshotAvailable(location: String, errorCode: Int) {
        // Allow app to display an indicator for (potentially) stale content
    }

    override fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String) {
        handler.cancel()
    }
}
