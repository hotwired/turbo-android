package com.basecamp.turbolinks

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.Presentation.*
import com.basecamp.turbolinks.PresentationContext.DEFAULT
import com.basecamp.turbolinks.PresentationContext.MODAL

class TurbolinksActivityDelegate(private val activity: TurbolinksActivity) : TurbolinksActivity by activity {
    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String, properties: PathProperties?): Boolean {
        val currentProperties = properties ?: currentPathConfiguration().properties(location)
        val currentContext = currentPresentationContext()
        val newContext = currentProperties.context
        val presentation = presentation(location, action)

        logEvent("navigate", "location" to location,
            "action" to action, "currentContext" to currentContext,
            "newContext" to newContext, "presentation" to presentation)

        when {
            presentation == NONE -> return false
            currentContext == newContext -> navigateWithinContext(location, currentProperties, presentation)
            newContext == MODAL -> navigateToModalContext(location)
            newContext == DEFAULT -> dismissModalContextWithResult(location)
        }

        return true
    }

    override fun navigateUp(): Boolean {
        detachWebViewFromCurrentDestination(destinationIsFinishing = true)
        return currentController().navigateUp()
    }

    override fun navigateBack() {
        popBackStack()
    }

    override fun clearBackStack() {
        if (isAtStartDestination()) return

        detachWebViewFromCurrentDestination(destinationIsFinishing = true) {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
        }
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun navigateWithinContext(location: String, properties: PathProperties, presentation: Presentation) {
        logEvent("navigateWithinContext", "location" to location, "presentation" to presentation)
        val bundle = buildBundle(location, presentation)

        detachWebViewFromCurrentDestination(destinationIsFinishing = presentation != PUSH) {
            if (presentation == POP || presentation == REPLACE) {
                currentController().popBackStack()
            }

            if (presentation == REPLACE || presentation == PUSH) {
                navigateToLocation(location, properties, bundle)
            }

            if (presentation == REPLACE_ALL) {
                clearBackStack()
            }
        }
    }

    private fun navigateToModalContext(location: String) {
        logEvent("navigateToModalContext", "location" to location)
        val bundle = buildBundle(location, PUSH)

        detachWebViewFromCurrentDestination(destinationIsFinishing = false) {
            onProvideRouter().getModalContextStartAction(location).let { actionId ->
                currentController().navigate(actionId, bundle)
            }
        }
    }

    private fun dismissModalContextWithResult(location: String) {
        logEvent("dismissModalContextWithResult", "location" to location)

        detachWebViewFromCurrentDestination(destinationIsFinishing = true) {
            val dismissAction = onProvideRouter().getModalContextDismissAction(location)
            sendModalResult(location, "advance")
            currentController().navigate(dismissAction)
        }
    }

    private fun sendModalResult(location: String, action: String) {
        currentDestinationAction {
            if (it is TurbolinksFragment) {
                it.onSetModalResult(TurbolinksModalResult(location, action))
            }
        }
    }

    private fun presentation(location: String, action: String): Presentation {
        val locationIsRoot = locationsAreSame(location, onProvideSessionRootLocation())
        val locationIsCurrent = locationsAreSame(location, currentLocation())
        val locationIsPrevious = locationsAreSame(location, previousLocation())
        val replace = action == "replace"

        return when {
            locationIsRoot && locationIsCurrent -> NONE
            locationIsPrevious -> POP
            locationIsRoot -> REPLACE_ALL
            locationIsCurrent || replace -> REPLACE
            else -> PUSH
        }
    }

    private fun navigateToLocation(location: String, properties: PathProperties, bundle: Bundle) {
        onProvideRouter().getNavigationAction(location, properties)?.let { actionId ->
            currentController().navigate(actionId, bundle)
        }
    }

    private fun currentController(): NavController {
        return currentDestination().findNavController()
    }

    private fun currentDestination(): Fragment {
        return onProvideCurrentNavHostFragment().childFragmentManager.primaryNavigationFragment ?:
            throw IllegalStateException("No current destination found in NavHostFragment")
    }

    private fun currentPathConfiguration(): PathConfiguration {
        return onProvideSession(currentDestination()).pathConfiguration
    }

    private fun currentPresentationContext(): PresentationContext {
        val location = currentDestinationArgument("location") ?: return DEFAULT
        return currentPathConfiguration().properties(location).context
    }

    private fun currentLocation(): String? {
        return currentDestinationArgument("location")
    }

    private fun currentDestinationArgument(key: String): String? {
        return currentDestination().arguments?.getString(key)
    }

    private fun previousLocation(): String? {
        return currentDestinationArgument("previousLocation")
    }

    private fun popBackStack() {
        detachWebViewFromCurrentDestination(destinationIsFinishing = true) {
            if (!currentController().popBackStack()) {
                onRequestFinish()
            }
        }
    }

    private fun isAtStartDestination(): Boolean {
        val controller = currentController()
        return controller.graph.startDestination == controller.currentDestination?.id
    }

    /**
     * It's necessary to detach the shared WebView from a screen *before* it is hidden or exits and
     * the navigation animations run. The framework animator expects that the View hierarchy will
     * not change during the transition. Because the incoming screen will attach the WebView to the
     * new view hierarchy, it needs to already be detached from the previous screen.
     */
    private fun detachWebViewFromCurrentDestination(destinationIsFinishing: Boolean, onDetached: () -> Unit = {}) {
        currentDestinationAction {
            when (it) {
                is TurbolinksFragment -> it.onProvideObserver().detachWebView(destinationIsFinishing, onDetached)
                else -> onDetached()
            }
        }
    }

    private fun currentDestinationAction(action: (Fragment) -> Unit) {
        currentDestination().let(action)
    }

    private fun locationsAreSame(first: String?, second: String?): Boolean {
        fun String.removeInconsequentialSuffix(): String {
            return this.removeSuffix("#").removeSuffix("/")
        }

        return first?.removeInconsequentialSuffix() == second?.removeInconsequentialSuffix()
    }

    private fun buildBundle(location: String, presentation: Presentation): Bundle {
        val previousLocation = when (presentation) {
            PUSH -> currentDestinationArgument("location")
            else -> currentDestinationArgument("previousLocation")
        }

        return bundleOf(
            "location" to location,
            "previousLocation" to previousLocation
        )
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        logEvent(event, params.toList())
    }
}
