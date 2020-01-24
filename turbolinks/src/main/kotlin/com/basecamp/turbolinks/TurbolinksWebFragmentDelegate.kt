package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.webkit.WebView
import kotlin.random.Random

@Suppress("unused")
open class TurbolinksWebFragmentDelegate(private val destination: TurbolinksDestination,
                                         private val callback: TurbolinksWebFragmentCallback) : TurbolinksSessionCallback {

    private var location = destination.location
    private var visitOptions = destination.visitOptions
    private val identifier = generateIdentifier()
    private var isInitialVisit = true
    private var isWebViewAttachedToNewDestination = false
    private var screenshot: Bitmap? = null
    private var screenshotOrientation = 0
    private val navigator: TurbolinksNavigator
        get() = destination.navigator
    private val turbolinksView: TurbolinksView?
        get() = callback.turbolinksView

    val webView: WebView?
        get() = session().webView

    fun onActivityCreated() {
        navigator.onNavigationVisit = { onReady ->
            detachWebView(onReady)
        }
    }

    fun onStart() {
        if (!destination.navigatedFromModalResult) {
            initNavigationVisit()
        }
    }

    fun session(): TurbolinksSession {
        return destination.session
    }

    // -----------------------------------------------------------------------
    // TurbolinksSessionCallback interface
    // -----------------------------------------------------------------------

    override fun onPageStarted(location: String) {
        callback.onColdBootPageStarted(location)
    }

    override fun onPageFinished(location: String) {
        callback.onColdBootPageCompleted(location)
    }

    override fun pageInvalidated() {}

    override fun visitLocationStarted(location: String) {
        callback.onVisitStarted(location)

        if (isWebViewAttachedToNewDestination) {
            showProgressView(location)
        }
    }

    override fun visitRendered() {
        destination.pageViewModel.setTitle(title())
        removeTransitionalViews()
    }

    override fun visitCompleted() {
        callback.onVisitCompleted(location)
        destination.pageViewModel.setTitle(title())
    }

    override fun onReceivedError(errorCode: Int) {
        callback.onVisitErrorReceived(location, errorCode)
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
        callback.onUpdateView()
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
            if (it) callback.onWebViewAttached()
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

        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onReady() }
        callback.onWebViewDetached()
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
        val progressView = callback.createProgressView(location)
        turbolinksView?.addProgressView(progressView)
    }

    private fun showErrorView(code: Int) {
        val errorView = callback.createErrorView(code)
        turbolinksView?.addErrorView(errorView)
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
