package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.TurbolinksSession.Companion.ACTION_ADVANCE
import kotlinx.android.synthetic.main.turbolinks_default.view.*
import kotlin.random.Random

private const val ARG_LOCATION = "location"

abstract class TurbolinksFragment : Fragment(), TurbolinksCallback {
    private lateinit var location: String
    private var identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private val turbolinksView: TurbolinksView?
        get() = view?.findViewById(R.id.turbolinks_view)
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = view?.findViewById(R.id.turbolinks_error_placeholder)

    protected open val pullToRefreshEnabled = true
    protected var listener: TurbolinksActivity? = null
    protected val webView: WebView?
        get() = session()?.webView

    abstract fun createView(): View
    abstract fun createErrorView(statusCode: Int): View
    abstract fun createProgressView(location: String): View
    abstract fun onDestinationTitleChanged(title: String)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        when (context) {
            is TurbolinksActivity -> listener = context
            else -> throw RuntimeException("$context must implement OnFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        location = arguments?.getString(ARG_LOCATION) ?:
                throw IllegalArgumentException("A location argument must be provided")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createView().apply {
            initializePullToRefresh(turbolinks_view)
            showScreenshotIfAvailable(turbolinks_view)
            screenshot = null
            screenshotOrientation = 0
        }
    }

    override fun onStart() {
        super.onStart()

        // Attempt to attach the WebView. It may already be attached to the current instance.
        isWebViewAttachedToNewDestination = attachWebView()

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

    fun attachWebView(): Boolean {
        return turbolinksView?.attachWebView(requireNotNull(webView)) ?: false
    }

    fun detachWebView(onDetached: () -> Unit) {
        val view = webView ?: return
        onDestinationTitleChanged("")
        screenshotView()
        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
    }

    // -----------------------------------------------------------------------
    // TurbolinksCallback interface
    // -----------------------------------------------------------------------

    override fun identifier(): Int {
        return identifier
    }

    override fun onPageStarted(location: String) {}

    override fun onPageFinished(location: String) {}

    override fun shouldOverrideUrl(location: String) {}

    override fun pageInvalidated() {}

    override fun visitRendered() {
        onDestinationTitleChanged(title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        onDestinationTitleChanged(title())
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

    override fun visitLocationStarted(location: String) {
        if (isWebViewAttachedToNewDestination) {
            showProgressView(location)
        }
    }

    override fun visitProposedToLocationWithAction(location: String, action: String) {
        onDestinationTitleChanged("")
        listener?.navigate(location, action)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun visit(location: String, restoreWithCachedSnapshot: Boolean = false) {
        val turbolinksSession = session() ?: return

        // Update the toolbar title while loading the next visit
        onDestinationTitleChanged("")

        turbolinksSession
                .callback(this)
                .restoreWithCachedSnapshot(restoreWithCachedSnapshot)
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
        val progressView = createProgressView(location)
        turbolinksView?.addProgressView(progressView)
    }

    private fun initializePullToRefresh(turbolinksView: TurbolinksView) {
        turbolinksView.refreshLayout.apply {
            isEnabled = pullToRefreshEnabled
            setOnRefreshListener {
                session()?.visitLocationWithAction(location, ACTION_ADVANCE)
            }
        }
    }

    private fun showScreenshotIfAvailable(turbolinksView: TurbolinksView) {
        if (screenshotOrientation == turbolinksView.screenshotOrientation()) {
            screenshot?.let { turbolinksView.addScreenshotView(it) }
        }
    }

    private fun removeTransitionalViews() {
        turbolinksView?.refreshLayout?.isRefreshing = false

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
        val errorView = createErrorView(code)

        turbolinksErrorPlaceholder?.removeAllViews()
        turbolinksErrorPlaceholder?.addView(errorView)
    }

    private fun generateIdentifier(): Int {
        return Random.nextInt(0, 999999999)
    }
}
