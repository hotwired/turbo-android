package com.basecamp.turbolinks

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

class TurbolinksNavigator(private val destination: TurbolinksDestination) {
    private val fragment = destination.fragment
    private val session = destination.session

    enum class PresentationContext {
        DEFAULT, MODAL
    }

    enum class Presentation {
        DEFAULT, PUSH, POP, REPLACE, REPLACE_ALL, REPLACE_ROOT, REFRESH, NONE
    }

    enum class NavigationMode {
        IN_CONTEXT, TO_MODAL, DISMISS_MODAL
    }

    var onNavigationVisit: (onNavigate: () -> Unit) -> Unit = { onReady ->
        destination.onBeforeNavigation()
        onReady()
    }

    fun navigateUp() {
        onNavigationVisit {
            currentController().navigateUp()
        }
    }

    fun navigateBack() {
        onNavigationVisit {
            if (!currentController().popBackStack()) {
                fragment.requireActivity().finish()
            }
        }
    }

    fun clearBackStack() {
        if (isAtStartDestination()) return

        onNavigationVisit {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
        }
    }

    fun navigate(location: String,
                 options: VisitOptions,
                 bundle: Bundle? = null,
                 extras: FragmentNavigator.Extras? = null) {

        if (!shouldNavigate(location)) {
            return
        }

        val rule = TurbolinksNavigatorRule(
                location = location,
                visitOptions = options,
                bundle = bundle,
                extras = extras,
                pathConfiguration = session.pathConfiguration,
                controller = currentControllerForLocation(location)
        )

        logEvent(
            "navigate", "location" to rule.newLocation,
            "options" to options, "currentContext" to rule.currentContext,
            "newContext" to rule.newContext, "presentation" to rule.newPresentation
        )

        when (rule.newNavigationMode) {
            NavigationMode.DISMISS_MODAL -> {
                dismissModalContextWithResult(rule)
            }
            NavigationMode.TO_MODAL -> {
                navigateToModalContext(rule)
            }
            NavigationMode.IN_CONTEXT -> {
                when (rule.newPresentation) {
                    Presentation.REFRESH -> {
                        // Refresh signals reloading the current destination
                        // url, ignoring the provided `location` url.
                        navigate(rule.currentLocation, VisitOptions())
                    }
                    else -> {
                        navigateWithinContext(rule)
                    }
                }
            }
        }
    }

    private fun navigateWithinContext(rule: TurbolinksNavigatorRule) {
        logEvent("navigateWithinContext", "location" to rule.newLocation, "presentation" to rule.newPresentation)

        when (rule.newPresentation) {
            Presentation.POP -> onNavigationVisit {
                rule.controller.popBackStack()
            }
            Presentation.REPLACE -> onNavigationVisit {
                rule.controller.popBackStack()
                navigateToLocation(rule)
            }
            Presentation.PUSH -> onNavigationVisit {
                navigateToLocation(rule)
            }
            Presentation.REPLACE_ROOT -> onNavigationVisit {
                replaceRootLocation(rule)
            }
            Presentation.REPLACE_ALL -> onNavigationVisit {
                clearBackStack()
            }
            Presentation.NONE -> {
                // Do nothing
            }
            else -> {
                throw IllegalStateException("Unexpected Presentation for navigating within context")
            }
        }
    }

    private fun navigateToModalContext(rule: TurbolinksNavigatorRule) {
        logEvent("navigateToModalContext", "location" to rule.newLocation)

        when (rule.newPresentation) {
            Presentation.REPLACE -> onNavigationVisit {
                rule.controller.popBackStack()
                navigateToLocation(rule)
            }
            else -> onNavigationVisit {
                navigateToLocation(rule)
            }
        }
    }

    private fun dismissModalContextWithResult(rule: TurbolinksNavigatorRule) {
        logEvent("dismissModalContextWithResult",
            "location" to rule.newLocation,
            "uri" to rule.newProperties.uri,
            "presentation" to rule.newPresentation
        )

        onNavigationVisit {
            if (destination.isDialog) {
                // Pop the backstack before sending the modal result, since the
                // underlying fragment is still active and will receive the
                // result immediately. This allows the modal result flow to
                // behave exactly like full screen fragments.
                rule.controller.popBackStack(rule.currentDestination.id, true)
                sendModalResult(rule)
            } else {
                sendModalResult(rule)
                rule.controller.popBackStack(rule.currentDestination.id, true)
            }
        }
    }

    private fun sendModalResult(rule: TurbolinksNavigatorRule) {
        // Save the modal result with VisitOptions so it can be retrieved
        // by the previous destination when the backstack is popped.
        rule.newModalResult?.let {
            destination.sessionViewModel.sendModalResult(it)
        }
    }

    private fun replaceRootLocation(rule: TurbolinksNavigatorRule) {
        if (rule.newDestination == null) {
            logEvent("replaceRootLocation", "error" to "No destination found")
            return
        }

        val navOptions = navOptions {
            popUpTo(rule.newDestination.id) { inclusive = true }
        }

        logEvent("replaceRootLocation", "location" to rule.newLocation, "uri" to rule.newProperties.uri)
        rule.controller.navigate(rule.newDestination.id, rule.newBundle, navOptions)
    }

    private fun navigateToLocation(rule: TurbolinksNavigatorRule) {
        val navOptions = navOptions(rule.newLocation, rule.newProperties)

        // Save the VisitOptions so it can be retrieved by the next
        // destination. When response.responseHTML is present it is
        // too large to save directly within the args bundle.
        destination.sessionViewModel.saveVisitOptions(rule.newVisitOptions)

        rule.newDestination?.let { destination ->
            logEvent("navigateToLocation", "location" to rule.newLocation, "uri" to rule.newDestinationUri)
            rule.controller.navigate(destination.id, rule.newBundle, navOptions, rule.newExtras)
            return
        }

        logEvent("navigateToLocation", "location" to rule.newLocation,
            "warning" to "No destination found", "uri" to rule.newProperties.uri)

        rule.newFallbackDestination?.let { destination ->
            logEvent("navigateToLocation", "location" to rule.newLocation, "fallbackUri" to "${rule.newFallbackUri}")
            rule.controller.navigate(destination.id, rule.newBundle, navOptions, rule.newExtras)
            return
        }

        logEvent("navigateToLocation", "location" to rule.newLocation,
            "error" to "No fallback destination found")
    }

    private fun currentController(): NavController {
        return fragment.findNavController()
    }

    private fun currentControllerForLocation(location: String): NavController {
        return destination.navHostForNavigation(location).navController
    }

    private fun isAtStartDestination(): Boolean {
        val controller = currentController()
        return controller.previousBackStackEntry == null
    }

    private fun shouldNavigate(location: String): Boolean {
        val shouldNavigate = destination.shouldNavigateTo(location)

        logEvent("shouldNavigateToLocation", "location" to location, "shouldNavigate" to shouldNavigate)
        return shouldNavigate
    }

    private fun navOptions(location: String, properties: PathProperties): NavOptions {
        return destination.getNavigationOptions(
            newLocation = location,
            newPathProperties = properties
        )
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to session.sessionName)
            add("fragment" to fragment.javaClass.simpleName)
        }
        logEvent(event, attributes)
    }
}
