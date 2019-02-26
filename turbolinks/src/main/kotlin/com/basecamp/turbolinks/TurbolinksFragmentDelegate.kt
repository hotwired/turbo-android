package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.turbolinks_default.view.*
import kotlin.random.Random

open class TurbolinksFragmentDelegate(fragment: TurbolinksFragment) : TurbolinksFragment by fragment, TurbolinksCallback {
    private lateinit var location: String
    private val identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private val turbolinksView: TurbolinksView?
        get() = onProvideTurbolinksView()
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = onProvideErrorPlaceholder()
    protected val webView: WebView?
        get() = onProvideSession()?.webView
    var activity: TurbolinksActivity? = null

    // ----------------------------------------------------------------------------
    // TurbolinksFragmentDelegate
    // ----------------------------------------------------------------------------

    open fun onWebViewAttached() {}

    open fun onWebViewDetached() {}

    fun create(arguments: Bundle?) {
        location = arguments?.getString("location") ?:
                throw IllegalArgumentException("A location argument must be provided")
    }

    fun attach(context: Context) {
        when (context) {
            is TurbolinksActivity -> activity = context
            else -> throw RuntimeException("$context must implement TurbolinksActivity")
        }
    }

    fun detach() {
        activity = null
    }

    fun createView(view: View) {
        view.apply {
            initializePullToRefresh(turbolinks_view)
            showScreenshotIfAvailable(turbolinks_view)
            screenshot = null
            screenshotOrientation = 0
        }
    }

    fun start() {
        onSetupToolbar()

        // Attempt to attach the WebView. It may already be attached to the current instance.
        isWebViewAttachedToNewDestination = attachWebView()

        // Visit every time the Fragment is attached to the Activity
        // or started again after visiting another Activity outside
        // of the main single-Activity architecture.
        visit(location, restoreWithCachedSnapshot = !isInitialVisit)
        isInitialVisit = false
    }

    fun title(): String {
        return webView?.title ?: ""
    }

    fun onProvideSession(fragment: Fragment): TurbolinksSession? {
        return activity?.onProvideSession(fragment)
    }

    // ----------------------------------------------------------------------------
    // TurbolinksFragment interface
    // ----------------------------------------------------------------------------

    override fun attachWebView(): Boolean {
        val view = turbolinksView ?: return false
        return view.attachWebView(requireNotNull(webView)).also {
            if (it) onWebViewAttached()
        }
    }

    override fun detachWebView(onDetached: () -> Unit) {
        val view = webView ?: return
        onTitleChanged("")
        screenshotView()
        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
        onWebViewDetached()
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
        onTitleChanged(title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        onTitleChanged(title())
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
        onTitleChanged("")
        activity?.navigate(location, action)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun visit(location: String, restoreWithCachedSnapshot: Boolean = false) {
        val turbolinksSession = onProvideSession() ?: return

        // Update the toolbar title while loading the next visit
        onTitleChanged("")

        turbolinksSession
                .callback(this)
                .restoreWithCachedSnapshot(restoreWithCachedSnapshot)
                .visit(location)
    }

    private fun screenshotView() {
        if (onProvideSession()?.enableScreenshots != true) return

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
            isEnabled = shouldEnablePullToRefresh()
            setOnRefreshListener {
                onProvideSession()?.visitLocationWithAction(location, TurbolinksSession.ACTION_ADVANCE)
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

    private fun handleError(code: Int) {
        val errorView = createErrorView(code)

        turbolinksErrorPlaceholder?.removeAllViews()
        turbolinksErrorPlaceholder?.addView(errorView)
    }

    private fun generateIdentifier(): Int {
        return Random.nextInt(0, 999999999)
    }
}
