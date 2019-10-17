package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.webkit.WebView
import kotlin.random.Random

@Suppress("unused")
open class TurbolinksWebFragmentDelegate(val fragment: TurbolinksWebFragment) : TurbolinksSessionCallback {

    private var location = fragment.location
    private var visitOptions = fragment.visitOptions
    private val identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private val navigator: TurbolinksNavigator
        get() = fragment.navigator
    private val turbolinksView: TurbolinksView?
        get() = fragment.turbolinksView

    val webView: WebView?
        get() = session().webView

    fun onActivityCreated() {
        navigator.onNavigationVisit = { onReady ->
            detachWebView(onReady)
        }
    }

    fun onStart() {
        if (!fragment.navigatedFromModalResult) {
            initNavigationVisit()
        }
    }

    fun session(): TurbolinksSession {
        return fragment.session
    }

    // -----------------------------------------------------------------------
    // TurbolinksSessionCallback interface
    // -----------------------------------------------------------------------

    override fun onPageStarted(location: String) {
        fragment.onColdBootPageStarted(location)
    }

    override fun onPageFinished(location: String) {
        fragment.onColdBootPageCompleted(location)
    }

    override fun pageInvalidated() {}

    override fun visitLocationStarted(location: String) {
        fragment.onVisitStarted(location)

        if (isWebViewAttachedToNewDestination) {
            showProgressView(location)
        }
    }

    override fun visitRendered() {
        fragment.pageViewModel.setTitle(title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        fragment.onVisitCompleted(location)
        fragment.pageViewModel.setTitle(title())
    }

    override fun onReceivedError(errorCode: Int) {
        fragment.onVisitErrorReceived(location, errorCode)
        showErrorView(errorCode)
    }

    override fun requestFailedWithStatusCode(statusCode: Int) {
        showErrorView(statusCode)
    }

    override fun visitProposedToLocation(location: String,
                                         options: VisitOptions,
                                         properties: PathProperties) {
        val navigated = navigator.navigate(location, options, properties)

        // In the case of a NONE presentation, reload the page with fresh data
        if (!navigated) {
            visit(location, restoreWithCachedSnapshot = false, reload = false)
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun initNavigationVisit() {
        initView()
        attachWebViewAndVisit()
    }

    private fun initView() {
        fragment.onUpdateView()
        turbolinksView?.apply {
            initializePullToRefresh(this)
            initializeErrorPullToRefresh(this)
            showScreenshotIfAvailable(this)
            screenshot = null
            screenshotOrientation = 0
        }
    }

    private fun attachWebView(): Boolean {
        val view = turbolinksView ?: return false
        return view.attachWebView(requireNotNull(webView)).also {
            if (it) fragment.onWebViewAttached()
        }
    }

    /**
     * It's necessary to detach the shared WebView from a screen *before* it is hidden or exits and
     * the navigation animations run. The framework animator expects that the View hierarchy will
     * not change during the transition. Because the incoming screen will attach the WebView to the
     * new view hierarchy, it needs to already be detached from the previous screen.
     */
    private fun detachWebView(onReady: () -> Unit = {}) {
        val view = webView ?: return
        screenshotView()

        // Clear the current toolbar title to prevent buggy animation
        // effect when transitioning to the next/previous screen.
        fragment.toolbar?.title = ""

        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onReady() }
        fragment.onWebViewDetached()
    }

    private fun attachWebViewAndVisit() {
        // Attempt to attach the WebView. It may already be attached to the current instance.
        isWebViewAttachedToNewDestination = attachWebView()

        // Visit every time the WebView is reattached to the current Fragment.
        if (isWebViewAttachedToNewDestination) {
            visit(location, restoreWithCachedSnapshot = !isInitialVisit, reload = false)
            isInitialVisit = false
        }
    }

    private fun title(): String {
        return webView?.title ?: ""
    }

    private fun visit(location: String, restoreWithCachedSnapshot: Boolean, reload: Boolean) {
        val restore = restoreWithCachedSnapshot && !reload
        val options = when {
            restore -> VisitOptions(action = VisitAction.RESTORE)
            reload -> VisitOptions()
            else -> visitOptions
        }

        session().visit(TurbolinksVisit(
                location = location,
                destinationIdentifier = identifier,
                restoreWithCachedSnapshot = restoreWithCachedSnapshot,
                reload = reload,
                callback = this,
                options = options
        ))
    }

    private fun screenshotView() {
        if (!session().enableScreenshots) return

        turbolinksView?.let {
            screenshot = it.createScreenshot()
            screenshotOrientation = it.screenshotOrientation()
            showScreenshotIfAvailable(it)
        }
    }

    private fun showProgressView(location: String) {
        val progressView = fragment.createProgressView(location)
        turbolinksView?.addProgressView(progressView)
    }

    private fun showErrorView(code: Int) {
        val errorView = fragment.createErrorView(code)
        turbolinksView?.addErrorView(errorView)
    }

    private fun initializePullToRefresh(turbolinksView: TurbolinksView) {
        turbolinksView.refreshLayout.apply {
            isEnabled = fragment.shouldEnablePullToRefresh()
            setOnRefreshListener {
                isWebViewAttachedToNewDestination = false
                visit(location, restoreWithCachedSnapshot = false, reload = true)
            }
        }
    }

    private fun initializeErrorPullToRefresh(turbolinksView: TurbolinksView) {
        turbolinksView.errorRefreshLayout.apply {
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
        turbolinksView?.errorRefreshLayout?.isRefreshing = false
        turbolinksView?.removeProgressView()
        turbolinksView?.removeScreenshot()
        turbolinksView?.removeErrorView()
    }

    private fun generateIdentifier(): Int {
        return Random.nextInt(0, 999999999)
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        logEvent(event, params.toList())
    }
}
