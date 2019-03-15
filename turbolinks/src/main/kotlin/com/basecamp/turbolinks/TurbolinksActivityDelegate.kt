package com.basecamp.turbolinks

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.Presentation.*
import com.basecamp.turbolinks.PresentationContext.MODAL

open class TurbolinksActivityDelegate(val activity: TurbolinksActivity) : TurbolinksActivity by activity {
    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String) {
        val presentationContext = onProvideRouter().getPresentationContext(location)
        val presentation = presentation(location, action)

        when (presentationContext) {
            MODAL -> navigateToModalContext(location)
            else -> navigateWithinContext(location, presentation)
        }
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

    protected fun navigateWithinContext(location: String, presentation: Presentation) {
        val bundle = buildBundle(location, presentation)

        if (presentation == NONE) {
            return
        }

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

    protected fun presentation(location: String, action: String): Presentation {
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

    private fun navigateToModalContext(location: String) {
        onStartModalContext(location)
    }

    private fun currentController(): NavController {
        return currentDestination().findNavController()
    }

    private fun currentDestination(): Fragment {
        return onProvideCurrentDestination()
    }

    private fun currentLocation(): String? {
        return currentDestination().arguments?.getString("location")
    }

    private fun previousLocation(): String? {
        return currentDestination().arguments?.getString("previousLocation")
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
        onProvideCurrentDestination().let(action)
    }

    private fun locationsAreSame(first: String?, second: String?): Boolean {
        fun String.removeInconsequentialSuffix(): String {
            return this.removeSuffix("#").removeSuffix("/")
        }

        return first?.removeInconsequentialSuffix() == second?.removeInconsequentialSuffix()
    }

    private fun buildBundle(location: String, presentation: Presentation): Bundle {
        val previousLocation = when (presentation) {
            PUSH -> currentDestination().arguments?.getString("location")
            else -> currentDestination().arguments?.getString("previousLocation")
        }

        return bundleOf(
            "location" to location,
            "previousLocation" to previousLocation
        )
    }
}
