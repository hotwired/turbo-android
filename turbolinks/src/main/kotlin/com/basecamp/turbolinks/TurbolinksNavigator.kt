package com.basecamp.turbolinks

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class TurbolinksNavigator(private val fragment: Fragment,
                          private val session: TurbolinksSession,
                          private val router: TurbolinksRouter,
                          private val onNavigationReady: (isFinishing: Boolean,
                                                          onReady: () -> Unit) -> Unit) {

    fun navigateUp(): Boolean {
        onNavigationReady(true) {}
        return currentController().navigateUp()
    }

    fun navigateBack() {
        popBackStack()
    }

    fun clearBackStack() {
        if (isAtStartDestination()) return

        onNavigationReady(true) {
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

        onNavigationReady(presentation != Presentation.PUSH) {
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

        onNavigationReady(false) {
            router.getModalContextStartAction(location).let { actionId ->
                currentController().navigate(actionId, bundle)
            }
        }
    }

    private fun dismissModalContextWithResult(location: String) {
        logEvent("dismissModalContextWithResult", "location" to location)

        onNavigationReady(true) {
            val dismissAction = router.getModalContextDismissAction(location)
            sendModalResult(location, "advance")
            currentController().navigate(dismissAction)
        }
    }

    private fun sendModalResult(location: String, action: String) {
        if (fragment is TurbolinksFragment) {
            fragment.sharedViewModel.modalResult = TurbolinksModalResult(location, action)
        }
    }

    private fun presentation(location: String, action: String): Presentation {
        val locationIsRoot = locationsAreSame(location, session.rootLocation)
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
        router.getNavigationAction(location, properties)?.let { actionId ->
            currentController().navigate(actionId, bundle)
        }
    }

    private fun currentController(): NavController {
        return fragment.findNavController()
    }

    private fun popBackStack() {
        onNavigationReady(true) {
            if (!currentController().popBackStack()) {
                fragment.requireActivity().finish()
            }
        }
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
        return session.pathConfiguration
    }

    private fun currentPresentationContext(): PresentationContext {
        val location = currentLocation() ?: return PresentationContext.DEFAULT
        return currentPathConfiguration().properties(location).context
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        logEvent(event, params.toList())
    }
}
