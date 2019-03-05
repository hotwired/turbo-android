package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.random.Random

open class TurbolinksFragmentObserver(fragment: TurbolinksFragment) :
        TurbolinksFragment by fragment, TurbolinksSessionCallback, LifecycleObserver {

    private lateinit var location: String
    private val identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private var activity: TurbolinksActivity? = null
    private val turbolinksView: TurbolinksView?
        get() = onProvideTurbolinksView()
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = onProvideErrorPlaceholder()
    private val fragment = fragment as? Fragment ?:
        throw IllegalArgumentException("fragment must be a Fragment")
    protected val webView: WebView?
        get() = session()?.webView

    // ----------------------------------------------------------------------------
    // TurbolinksFragmentObserver
    // ----------------------------------------------------------------------------

    open fun onWebViewAttached() {}

    open fun onWebViewDetached() {}

    @OnLifecycleEvent(ON_CREATE)
    private fun create() {
        location = fragment.arguments?.getString("location") ?:
                throw IllegalArgumentException("A location argument must be provided")
    }

    @OnLifecycleEvent(ON_START)
    private fun start() {
        activity = fragment.context as? TurbolinksActivity ?:
                throw RuntimeException("The fragment Activity must implement TurbolinksActivity")

        initView()
        attachWebViewAndVisit()
    }

    @OnLifecycleEvent(ON_STOP)
    private fun stop() {
        activity = null
    }

    fun session(): TurbolinksSession? {
        return activity?.onProvideSession(fragment)
    }

    fun attachWebView(): Boolean {
        val view = turbolinksView ?: return false
        return view.attachWebView(requireNotNull(webView)).also {
            if (it) onWebViewAttached()
        }
    }

    fun detachWebView(destinationIsFinishing: Boolean, onDetached: () -> Unit) {
        val view = webView ?: return
        if (!destinationIsFinishing) {
            screenshotView()
        }

        onTitleChanged("")
        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
        onWebViewDetached()
    }

    fun navigateBack() {
        activity?.navigateBack()
    }

    // -----------------------------------------------------------------------
    // TurbolinksCallback interface
    // -----------------------------------------------------------------------

    override fun onPageStarted(location: String) {}

    override fun onPageFinished(location: String) {}

    override fun pageInvalidated() {}

    override fun shouldOverrideUrl(location: String): Boolean {
        return false
    }

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

    private fun initView() {
        onSetupToolbar()
        turbolinksView?.apply {
            initializePullToRefresh(this)
            showScreenshotIfAvailable(this)
            screenshot = null
            screenshotOrientation = 0
        }
    }

    private fun attachWebViewAndVisit() {
        // Attempt to attach the WebView. It may already be attached to the current instance.
        isWebViewAttachedToNewDestination = attachWebView()

        // Visit every time the Fragment is attached to the Activity
        // or started again after visiting another Activity outside
        // of the main single-Activity architecture.
        visit(location, restoreWithCachedSnapshot = !isInitialVisit, reload = false)
        isInitialVisit = false
    }

    private fun title(): String {
        return webView?.title ?: ""
    }

    private fun visit(location: String, restoreWithCachedSnapshot: Boolean, reload: Boolean) {
        val turbolinksSession = session() ?: return

        // Update the toolbar title while loading the next visit
        if (!reload) {
            onTitleChanged("")
        }

        turbolinksSession.visit(TurbolinksVisit(
                location = location,
                destinationIdentifier = identifier,
                restoreWithCachedSnapshot = restoreWithCachedSnapshot,
                reload = reload,
                callback = this
        ))
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
            isEnabled = shouldEnablePullToRefresh()
            setOnRefreshListener {
                isWebViewAttachedToNewDestination = false
                visit(location, restoreWithCachedSnapshot = false, reload = true)
            }
        }
    }

    private fun showScreenshotIfAvailable(turbolinksView: TurbolinksView) {
        if (screenshotOrientation == turbolinksView.screenshotOrientation()) {
            screenshot?.let { turbolinksView.addScreenshot(it) }
        }
    }

    private fun removeTransitionalViews() {
        turbolinksView?.refreshLayout?.isRefreshing = false

        // TODO: This delay shouldn't be necessary, but visitRendered() is being called early.
        delay(200) {
            turbolinksView?.removeProgressView()
            turbolinksView?.removeScreenshot()
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
