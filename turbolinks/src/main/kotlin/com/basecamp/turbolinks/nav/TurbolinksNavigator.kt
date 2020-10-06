package com.basecamp.turbolinks.nav

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.core.TurbolinksDestination
import com.basecamp.turbolinks.core.VisitOptions
import com.basecamp.turbolinks.nav.TurbolinksNavRule.NavigationMode
import com.basecamp.turbolinks.nav.TurbolinksNavRule.Presentation
import com.basecamp.turbolinks.util.logEvent

class TurbolinksNavigator(private val destination: TurbolinksDestination) {
    private val fragment = destination.fragment
    private val session = destination.session

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

    fun navigate(
        location: String,
        options: VisitOptions,
        bundle: Bundle? = null,
        extras: FragmentNavigator.Extras? = null
    ) {

        if (!shouldNavigate(location)) {
            return
        }

        val rule = TurbolinksNavRule(
            location = location,
            visitOptions = options,
            bundle = bundle,
            navOptions = navOptions(location),
            extras = extras,
            pathConfiguration = session.pathConfiguration,
            controller = currentControllerForLocation(location)
        )

        logEvent(
            "navigate", "location" to rule.newLocation,
            "options" to options,
            "currentContext" to rule.currentPresentationContext,
            "newContext" to rule.newPresentationContext,
            "presentation" to rule.newPresentation
        )

        when (rule.newNavigationMode) {
            NavigationMode.DISMISS_MODAL -> {
                dismissModalContextWithResult(rule)
            }
            NavigationMode.TO_MODAL -> {
                navigateToModalContext(rule)
            }
            NavigationMode.IN_CONTEXT -> {
                navigateWithinContext(rule)
            }
            NavigationMode.REFRESH -> {
                navigate(rule.currentLocation, VisitOptions())
            }
            NavigationMode.NONE -> {
                // Do nothing
            }
        }
    }

    private fun navigateWithinContext(rule: TurbolinksNavRule) {
        logEvent(
            "navigateWithinContext",
            "location" to rule.newLocation,
            "presentation" to rule.newPresentation
        )

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
            else -> {
                throw IllegalStateException("Unexpected Presentation for navigating within context")
            }
        }
    }

    private fun navigateToModalContext(rule: TurbolinksNavRule) {
        logEvent(
            "navigateToModalContext",
            "location" to rule.newLocation
        )

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

    private fun dismissModalContextWithResult(rule: TurbolinksNavRule) {
        logEvent(
            "dismissModalContextWithResult",
            "location" to rule.newLocation,
            "uri" to rule.newDestinationUri,
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

    private fun sendModalResult(rule: TurbolinksNavRule) {
        // Save the modal result with VisitOptions so it can be retrieved
        // by the previous destination when the backstack is popped.
        destination.sessionViewModel.sendModalResult(
            checkNotNull(rule.newModalResult)
        )
    }

    private fun replaceRootLocation(rule: TurbolinksNavRule) {
        if (rule.newDestination == null) {
            logEvent(
                "replaceRootLocation",
                "location" to rule.newLocation,
                "error" to "No destination found",
                "uri" to rule.newDestinationUri
            )
            return
        }

        logEvent(
            "replaceRootLocation",
            "location" to rule.newLocation,
            "uri" to rule.newDestinationUri
        )
        rule.controller.navigate(rule.newDestination.id, rule.newBundle, rule.newNavOptions)
    }

    private fun navigateToLocation(rule: TurbolinksNavRule) {
        // Save the VisitOptions so it can be retrieved by the next
        // destination. When response.responseHTML is present it is
        // too large to save directly within the args bundle.
        destination.sessionViewModel.saveVisitOptions(rule.newVisitOptions)

        rule.newDestination?.let {
            logEvent(
                "navigateToLocation",
                "location" to rule.newLocation,
                "uri" to rule.newDestinationUri
            )
            rule.controller.navigate(it.id, rule.newBundle, rule.newNavOptions, rule.newExtras)
            return
        }

        logEvent(
            "navigateToLocation",
            "location" to rule.newLocation,
            "warning" to "No destination found",
            "uri" to rule.newDestinationUri
        )

        rule.newFallbackDestination?.let {
            logEvent(
                "navigateToLocation",
                "location" to rule.newLocation,
                "fallbackUri" to "${rule.newFallbackUri}"
            )
            rule.controller.navigate(it.id, rule.newBundle, rule.newNavOptions, rule.newExtras)
            return
        }

        logEvent(
            "navigateToLocation",
            "location" to rule.newLocation,
            "error" to "No fallback destination found"
        )
    }

    private fun currentController(): NavController {
        return fragment.findNavController()
    }

    private fun currentControllerForLocation(location: String): NavController {
        return destination.navHostForNavigation(location).navController
    }

    private fun isAtStartDestination(): Boolean {
        return currentController().previousBackStackEntry == null
    }

    private fun shouldNavigate(location: String): Boolean {
        val shouldNavigate = destination.shouldNavigateTo(location)

        logEvent(
            "shouldNavigateToLocation",
            "location" to location,
            "shouldNavigate" to shouldNavigate
        )
        return shouldNavigate
    }

    private fun navOptions(location: String): NavOptions {
        val properties = session.pathConfiguration.properties(location)

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
