package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.turbolinks_default.view.*

private const val ARG_LOCATION = "location"

abstract class TurbolinksFragment : Fragment(), TurbolinksCallback {
    private lateinit var location: String
    private var isInitialVisit = true
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private val turbolinksView: TurbolinksView?
        get() = view?.findViewById(R.id.turbolinks_view)
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = view?.findViewById(R.id.turbolinks_error_placeholder)

    protected var listener: OnFragmentListener? = null
    protected val webView: WebView?
        get() = session()?.webView

    abstract fun initialUrl(): String

    abstract fun createView(): View

    override fun onAttach(context: Context) {
        super.onAttach(context)

        when (context) {
            is OnFragmentListener -> listener = context
            else -> throw RuntimeException("$context must implement OnFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        location = arguments?.getString(ARG_LOCATION) ?: initialUrl()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createView().apply {
            showScreenshotIfAvailable(turbolinks_view)
            screenshot = null
            screenshotOrientation = 0
        }
    }

    override fun onStart() {
        super.onStart()

        // Visit every time the Fragment is attached to the Activity
        // or started again after visiting another Activity outside
        // of the main single-Activity architecture.
        visit(location, restoreWithCachedSnapshot = !isInitialVisit)
        isInitialVisit = false
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun title(): String {
        return webView?.title ?: ""
    }

    fun detachWebView(onDetached: () -> Unit) {
        val view = webView ?: return
        screenshotView()
        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
    }

    interface OnFragmentListener {
        fun onProvideSession(fragment: TurbolinksFragment): TurbolinksSession
        fun onProvideProgressView(location: String): View
        fun onProvideErrorView(errorStatusCode : Int): View
        fun onDestinationTitleChanged(fragment: Fragment, title: String)
        fun onRequestFullscreen()
        fun onRequestExitFullscreen()
        fun navigate(location: String, action: String)
        fun popBackStack()
    }

    // -----------------------------------------------------------------------
    // TurbolinksCallback interface
    // -----------------------------------------------------------------------

    override fun onPageStarted(location: String) {}

    override fun onPageFinished(location: String) {}

    override fun shouldOverrideUrl(location: String) {}

    override fun pageInvalidated() {}

    override fun visitRendered() {
        listener?.onDestinationTitleChanged(this, title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        listener?.onDestinationTitleChanged(this, title())
        removeTransitionalViews()
    }

    override fun onReceivedError(errorCode: Int) {
        handleError(errorCode)
        removeTransitionalViews()
    }

    override fun requestFailedWithStatusCode(statusCode: Int) {
        handleError(statusCode)
        removeTransitionalViews()
    }

    override fun visitLocationWithNewDestinationStarted(location: String) {
        showProgressView(location)
    }

    override fun visitProposedToLocationWithAction(location: String, action: String) {
        listener?.onDestinationTitleChanged(this, "")
        listener?.navigate(location, action)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun visit(location: String, restoreWithCachedSnapshot: Boolean = false) {
        val turbolinksSession = session() ?: return

        // Update the toolbar title while loading the next visit
        listener?.onDestinationTitleChanged(this, "")

        turbolinksSession
                .fragment(this)
                .callback(this)
                .restoreWithCachedSnapshot(restoreWithCachedSnapshot)
                .view(requireNotNull(turbolinksView))
                .visit(location)
    }

    private fun screenshotView() {
        if (session()?.enableScreenshots != true) return

        turbolinksView?.let {
            screenshot = it.createScreenshot()
            screenshotOrientation = it.screenshotOrientation()
            showScreenshotIfAvailable(it)
        }
    }

    private fun showProgressView(location: String) {
        val progressView = listener?.onProvideProgressView(location) ?: return
        turbolinksView?.addProgressView(progressView)
    }

    private fun showScreenshotIfAvailable(turbolinksView: TurbolinksView) {
        if (screenshotOrientation == turbolinksView.screenshotOrientation()) {
            screenshot?.let { turbolinksView.addScreenshotView(it) }
        }
    }

    private fun removeTransitionalViews() {
        // TODO: This delay shouldn't be necessary, but visitRendered() is being called early.
        delay(200) {
            turbolinksView?.removeProgressView()
            turbolinksView?.removeScreenshotView()
        }
    }

    private fun session(): TurbolinksSession? {
        return listener?.onProvideSession(this)
    }

    private fun handleError(code: Int) {
        val errorView = listener?.onProvideErrorView(code) ?: return

        turbolinksErrorPlaceholder?.removeAllViews()
        turbolinksErrorPlaceholder?.addView(errorView)
    }
}
