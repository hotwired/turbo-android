package com.basecamp.turbolinks

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.Presentation.*
import com.basecamp.turbolinks.PresentationContext.DEFAULT
import com.basecamp.turbolinks.PresentationContext.MODAL

class TurbolinksActivityDelegate(val activity: TurbolinksActivity) : TurbolinksActivity by activity {
    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String): Boolean {
        val currentContext = currentPresentationContext()
        val newContext = onProvideRouter().getPresentationContext(location)
        val presentation = presentation(location, action)

        when {
            presentation == NONE -> return false
            currentContext == newContext -> navigateWithinContext(location, presentation)
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
    // Protected
    // ----------------------------------------------------------------------------

    private fun navigateWithinContext(location: String, presentation: Presentation) {
        val bundle = buildBundle(location, presentation)

        TurbolinksLog.e("navigateWithinContext() location: $location, presentation: $presentation")

        detachWebViewFromCurrentDestination(destinationIsFinishing = presentation != PUSH) {
            if (presentation == POP || presentation == REPLACE) {
                currentController().popBackStack()
            }

            if (presentation == REPLACE || presentation == PUSH) {
                navigateToLocation(location, bundle)
            }

            if (presentation == REPLACE_ALL) {
                clearBackStack()
            }
        }
    }

    private fun navigateToModalContext(location: String) {
        val bundle = buildBundle(location, PUSH)

        TurbolinksLog.e("navigateToModalContext() location: $location")

        detachWebViewFromCurrentDestination(destinationIsFinishing = false) {
            onProvideRouter().getModalContextStartAction(location).let { actionId ->
                currentController().navigate(actionId, bundle)
            }
        }
    }

    private fun dismissModalContextWithResult(location: String) {
        TurbolinksLog.e("dismissModalContextAndNavigate() location: $location")

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
        TurbolinksLog.e("presentation() currentLocation: ${currentLocation()}, previousLocation: ${previousLocation()}")

        val locationIsRoot = locationsAreSame(location, onProvideSessionRootLocation())
        val locationIsCurrent = locationsAreSame(location, currentLocation())
        val locationIsPrevious = locationsAreSame(location, previousLocation())
        val shouldPop = action == "replace"

        return when {
            locationIsRoot && locationIsCurrent -> NONE
            shouldPop && locationIsPrevious -> POP
            locationIsRoot -> REPLACE_ALL
            shouldPop || locationIsCurrent -> REPLACE
            else -> PUSH
        }
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun navigateToLocation(location: String, bundle: Bundle) {
        onProvideRouter().getNavigationAction(location)?.let { actionId ->
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

    private fun currentPresentationContext(): PresentationContext {
        val location = currentDestinationArgument("location") ?: return DEFAULT
        return onProvideRouter().getPresentationContext(location)
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
}
