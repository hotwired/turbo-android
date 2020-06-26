package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import kotlin.random.Random

class TurbolinksWebFragmentDelegate(private val destination: TurbolinksDestination,
                                    private val callback: TurbolinksWebFragmentCallback) : TurbolinksSessionCallback {

    private val location = destination.location
    private val visitOptions = currentVisitOptions()
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
        if (session().isRenderProcessGone) {
            destination.navHost.createNewSession()
        }

        navigator.onNavigationVisit = { onReady ->
            destination.onBeforeNavigation()
            detachWebView(onReady)
        }
    }

    fun onStart() {
        initNavigationVisit()
    }

    fun onStartAfterModalResult(result: TurbolinksModalResult) {
        if (!result.shouldNavigate) {
            initNavigationVisit()
        }
    }

    fun onStartAfterDialogCancel() {
        initNavigationVisit()
    }

    fun onDialogCancel() {
        detachWebView()
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

    override fun visitCompleted(completedOffline: Boolean) {
        callback.onVisitCompleted(location, completedOffline)
        destination.pageViewModel.setTitle(title())
    }

    override fun onReceivedError(errorCode: Int) {
        callback.onVisitErrorReceived(location, errorCode)
        showErrorView(errorCode)
    }

    override fun onRenderProcessGone() {
        navigator.navigate(location, VisitOptions(action = VisitAction.REPLACE))
    }

    override fun requestFailedWithStatusCode(statusCode: Int) {
        showErrorView(statusCode)
    }

    override fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String) {
        callback.onReceivedHttpAuthRequest(handler, host, realm)
    }

    override fun visitProposedToLocation(location: String,
                                         options: VisitOptions) {
        navigator.navigate(location, options)
    }

    override fun isActive(): Boolean {
        return destination.fragment.isAdded
    }

    // TODO: New callback to give app an option to respond to shouldInterceptRequest
    override fun shouldInterceptRequest(location: String) {
        callback.shouldInterceptRequest(location)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun currentVisitOptions(): VisitOptions {
        return destination.sessionViewModel.visitOptions?.getContentIfNotHandled() ?: VisitOptions()
    }

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

    private fun attachWebView(onReady: (Boolean) -> Unit = {}) {
        val view = turbolinksView

        if (view == null) {
            onReady(false)
            return
        }

        view.attachWebView(requireNotNull(webView)) { attachedToNewDestination ->
            onReady(attachedToNewDestination)

            if (attachedToNewDestination) {
                callback.onWebViewAttached()
            }
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

        turbolinksView?.detachWebView(view) {
            onReady()
            callback.onWebViewDetached()
        }
    }

    private fun attachWebViewAndVisit() {
        // Attempt to attach the WebView. It may already be attached to the current instance.
        attachWebView {
            isWebViewAttachedToNewDestination = it

            // Visit every time the WebView is reattached to the current Fragment.
            if (isWebViewAttachedToNewDestination) {
                showProgressView(location)
                visit(location, restoreWithCachedSnapshot = !isInitialVisit, reload = false)
                isInitialVisit = false
            }
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
        turbolinksView.webViewRefresh?.apply {
            isEnabled = callback.shouldEnablePullToRefresh()
            setOnRefreshListener {
                isWebViewAttachedToNewDestination = false
                visit(location, restoreWithCachedSnapshot = false, reload = true)
            }
        }
    }

    private fun initializeErrorPullToRefresh(turbolinksView: TurbolinksView) {
        turbolinksView.errorRefresh?.apply {
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
        turbolinksView?.webViewRefresh?.isRefreshing = false
        turbolinksView?.errorRefresh?.isRefreshing = false
        turbolinksView?.removeProgressView()
        turbolinksView?.removeScreenshot()
        turbolinksView?.removeErrorView()
    }

    private fun generateIdentifier(): Int {
        return Random.nextInt(0, 999999999)
    }
}
