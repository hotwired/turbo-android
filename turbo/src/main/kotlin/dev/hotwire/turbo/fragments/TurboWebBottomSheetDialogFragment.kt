package dev.hotwire.turbo.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import dev.hotwire.turbo.R
import dev.hotwire.turbo.delegates.TurboWebFragmentDelegate
import dev.hotwire.turbo.util.TURBO_REQUEST_CODE_FILES
import dev.hotwire.turbo.views.TurboView
import dev.hotwire.turbo.views.TurboWebChromeClient

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webDelegate.onViewCreated()
    }

    override fun activityResultLauncher(requestCode: Int): ActivityResultLauncher<Intent>? {
        return when (requestCode) {
            TURBO_REQUEST_CODE_FILES -> webDelegate.fileChooserResultLauncher
            else -> null
        }
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

    override fun refresh(displayProgress: Boolean) {
        webDelegate.refresh(displayProgress)
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
        return layoutInflater.inflate(R.layout.turbo_error, null)
    }

    override fun createWebChromeClient(): TurboWebChromeClient {
        return TurboWebChromeClient(session)
    }

    override fun onVisitErrorReceived(location: String, errorCode: Int) {
        webDelegate.showErrorView(errorCode)
    }
}
