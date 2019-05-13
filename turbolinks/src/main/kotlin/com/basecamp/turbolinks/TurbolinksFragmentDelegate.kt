package com.basecamp.turbolinks

import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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
    private val turbolinksView: TurbolinksView?
        get() = callback.onProvideTurbolinksView()
    private val turbolinksErrorPlaceholder: ViewGroup?
        get() = callback.onProvideErrorPlaceholder()

    val webView: WebView?
        get() = session().webView

    fun onCreate(location: String) {
        this.location = location
    }

    fun onStart() {
        initNavigationVisit()
    }

    fun session(): TurbolinksSession {
        return fragment.session
    }

    fun navigateUp(): Boolean {
        detachWebView(destinationIsFinishing = true)
        return currentController().navigateUp()
    }

    fun navigateBack() {
        popBackStack()
    }

    fun clearBackStack() {
        if (isAtStartDestination()) return

        detachWebView(destinationIsFinishing = true) {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
        }
    }

    fun navigate(location: String, action: String, properties: PathProperties? = null): Boolean {
        val currentProperties = properties ?: currentPathConfiguration().properties(location)
        val currentContext = currentPresentationContext()
        val newContext = currentProperties.context
        val presentation = presentation(location, action)

        logEvent("navigate", "location" to location,
            "action" to action, "currentContext" to currentContext,
            "newContext" to newContext, "presentation" to presentation)

        when {
            presentation == Presentation.NONE -> return false
            currentContext == newContext -> navigateWithinContext(location, currentProperties, presentation)
            newContext == PresentationContext.MODAL -> navigateToModalContext(location)
            newContext == PresentationContext.DEFAULT -> dismissModalContextWithResult(location)
        }

        return true
    }

    private fun navigateWithinContext(location: String, properties: PathProperties, presentation: Presentation) {
        logEvent("navigateWithinContext", "location" to location, "presentation" to presentation)
        val bundle = buildBundle(location, presentation)

        detachWebView(destinationIsFinishing = presentation != Presentation.PUSH) {
            if (presentation == Presentation.POP || presentation == Presentation.REPLACE) {
                currentController().popBackStack()
            }

            if (presentation == Presentation.REPLACE || presentation == Presentation.PUSH) {
                navigateToLocation(location, properties, bundle)
            }

            if (presentation == Presentation.REPLACE_ALL) {
                clearBackStack()
            }
        }
    }

    private fun navigateToModalContext(location: String) {
        logEvent("navigateToModalContext", "location" to location)
        val bundle = buildBundle(location, Presentation.PUSH)

        detachWebView(destinationIsFinishing = false) {
            fragment.router.getModalContextStartAction(location).let { actionId ->
                currentController().navigate(actionId, bundle)
            }
        }
    }

    private fun dismissModalContextWithResult(location: String) {
        logEvent("dismissModalContextWithResult", "location" to location)

        detachWebView(destinationIsFinishing = true) {
            val dismissAction = fragment.router.getModalContextDismissAction(location)
            sendModalResult(location, "advance")
            currentController().navigate(dismissAction)
        }
    }

    private fun sendModalResult(location: String, action: String) {
        fragment.sharedViewModel.modalResult = TurbolinksModalResult(location, action)
    }

    private fun presentation(location: String, action: String): Presentation {
        val locationIsRoot = locationsAreSame(location, session().rootLocation)
        val locationIsCurrent = locationsAreSame(location, currentLocation())
        val locationIsPrevious = locationsAreSame(location, previousLocation())
        val replace = action == "replace"

        return when {
            locationIsRoot && locationIsCurrent -> Presentation.NONE
            locationIsPrevious -> Presentation.POP
            locationIsRoot -> Presentation.REPLACE_ALL
            locationIsCurrent || replace -> Presentation.REPLACE
            else -> Presentation.PUSH
        }
    }

    private fun navigateToLocation(location: String, properties: PathProperties, bundle: Bundle) {
        fragment.router.getNavigationAction(location, properties)?.let { actionId ->
            currentController().navigate(actionId, bundle)
        }
    }

    private fun currentController(): NavController {
        return fragment.findNavController()
    }

    private fun popBackStack() {
        detachWebView(destinationIsFinishing = true) {
            if (!currentController().popBackStack()) {
                fragment.requireActivity().finish()
            }
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
    private fun detachWebView(destinationIsFinishing: Boolean, onDetached: () -> Unit = {}) {
        val view = webView ?: return
        if (!destinationIsFinishing) {
            screenshotView()
        }

        // Clear the current toolbar title to prevent buggy animation
        // effect when transitioning to the next/previous screen.
        fragment.onProvideToolbar()?.title = ""

        turbolinksView?.detachWebView(view)
        turbolinksView?.post { onDetached() }
        callback.onWebViewDetached()
    }

    private fun isAtStartDestination(): Boolean {
        val controller = currentController()
        return controller.graph.startDestination == controller.currentDestination?.id
    }

    private fun locationsAreSame(first: String?, second: String?): Boolean {
        fun String.removeInconsequentialSuffix(): String {
            return this.removeSuffix("#").removeSuffix("/")
        }

        return first?.removeInconsequentialSuffix() == second?.removeInconsequentialSuffix()
    }

    private fun buildBundle(location: String, presentation: Presentation): Bundle {
        val previousLocation = when (presentation) {
            Presentation.PUSH -> currentLocation()
            else -> previousLocation()
        }

        return bundleOf(
            "location" to location,
            "previousLocation" to previousLocation
        )
    }

    private fun currentLocation(): String? {
        return fragment.arguments?.getString("location")
    }

    private fun previousLocation(): String? {
        return fragment.arguments?.getString("previousLocation")
    }

    private fun currentPathConfiguration(): PathConfiguration {
        return session().pathConfiguration
    }

    private fun currentPresentationContext(): PresentationContext {
        val location = currentLocation() ?: return PresentationContext.DEFAULT
        return currentPathConfiguration().properties(location).context
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
        val navigated = navigate(location, action, properties)

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
            navigate(it.location, it.action)
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
        session().visit(TurbolinksVisit(
                location = location,
                destinationIdentifier = identifier,
                restoreWithCachedSnapshot = restoreWithCachedSnapshot,
                reload = reload,
                callback = this
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

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        logEvent(event, params.toList())
    }
}
