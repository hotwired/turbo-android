package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import kotlin.random.Random

@Suppress("unused")
open class TurbolinksFragmentDelegate(val fragment: TurbolinksFragment,
                                      val callback: TurbolinksFragmentCallback) : TurbolinksSessionCallback {

    private lateinit var location: String
    private val identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private var activity: TurbolinksActivity? = null
    private val turbolinksView: TurbolinksView?
        get() = callback.onProvideTurbolinksView()
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = callback.onProvideErrorPlaceholder()

    val webView: WebView?
        get() = session()?.webView

    fun onCreate(location: String) {
        this.location = location
    }

    fun onStart(activity: TurbolinksActivity) {
        this.activity = activity
        initNavigationVisit()
    }

    fun onStop() {
        this.activity = null
    }

    fun session(): TurbolinksSession? {
        return activity?.onProvideSession(fragment)
    }

    fun attachWebView(): Boolean {
        val view = turbolinksView ?: return false
        return view.attachWebView(requireNotNull(webView)).also {
            if (it) callback.onWebViewAttached()
        }
    }

    fun detachWebView(destinationIsFinishing: Boolean, onDetached: () -> Unit) {
        val view = webView ?: return
        if (!destinationIsFinishing) {
            screenshotView()
        }

        fragment.viewModel.setTitle("")
        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
        callback.onWebViewDetached()
    }

    fun navigate(location: String, action: String = "advance") {
        activity?.navigate(location, action)
    }

    fun navigateUp() {
        activity?.navigateUp()
    }

    fun navigateBack() {
        activity?.navigateBack()
    }

    // -----------------------------------------------------------------------
    // TurbolinksSessionCallback interface
    // -----------------------------------------------------------------------

    override fun onPageStarted(location: String) {
        callback.onColdBootPageStarted(location)
    }

    override fun onPageFinished(location: String) {
        callback.onColdBootPageFinished(location)
    }

    override fun pageInvalidated() {}

    override fun visitRendered() {
        fragment.viewModel.setTitle(title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        fragment.viewModel.setTitle(title())
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

    override fun visitProposedToLocation(location: String, action: String,
                                         properties: PathProperties) {
        val navigated = activity?.navigate(location, action, properties)

        // In the case of a NONE presentation, reload the page with fresh data
        if (navigated == false) {
            visit(location, restoreWithCachedSnapshot = false, reload = false)
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun initNavigationVisit() {
        val navigated = fragment.sharedViewModel.modalResult?.let {
            activity?.navigate(it.location, it.action)
        } ?: false

        if (!navigated) {
            initView()
            attachWebViewAndVisit()
        }
    }

    private fun initView() {
        callback.onSetupToolbar()
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
        val progressView = callback.createProgressView(location)
        turbolinksView?.addProgressView(progressView)
    }

    private fun initializePullToRefresh(turbolinksView: TurbolinksView) {
        turbolinksView.refreshLayout.apply {
            isEnabled = callback.shouldEnablePullToRefresh()
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
        val errorView = callback.createErrorView(code)

        // Make sure the underlying WebView isn't clickable.
        errorView.isClickable = true

        turbolinksErrorPlaceholder?.removeAllViews()
        turbolinksErrorPlaceholder?.addView(errorView)
    }

    private fun generateIdentifier(): Int {
        return Random.nextInt(0, 999999999)
    }
}
