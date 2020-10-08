package com.basecamp.turbolinks.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.webkit.HttpAuthHandler
import com.basecamp.turbolinks.delegates.TurbolinksWebFragmentDelegate
import com.basecamp.turbolinks.util.TurbolinksWebFragmentCallback

@Suppress("unused")
abstract class TurbolinksWebBottomSheetDialogFragment : TurbolinksBottomSheetDialogFragment(), TurbolinksWebFragmentCallback {
    private lateinit var delegate: TurbolinksWebFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksWebFragmentDelegate(this, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    override fun onBeforeNavigation() {
        // Allow subclasses to do state cleanup
    }

    // ----------------------------------------------------------------------------
    // TurbolinksWebFragmentCallback interface
    // ----------------------------------------------------------------------------

    override fun onUpdateView() {}

    override fun onWebViewAttached() {}

    override fun onWebViewDetached() {}

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
