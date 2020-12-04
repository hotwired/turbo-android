package com.basecamp.turbolinks.nav

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.util.logEvent
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

internal class TurbolinksNavigator(private val navDestination: TurbolinksNavDestination) {
    private val fragment = navDestination.fragment
    private val session = navDestination.session

    var onNavigationVisit: (onNavigate: () -> Unit) -> Unit = { onReady ->
        navDestination.onBeforeNavigation()
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

    fun navigate(
        location: String,
        options: TurbolinksVisitOptions,
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
            TurbolinksNavMode.DISMISS_MODAL -> {
                dismissModalContextWithResult(rule)
            }
            TurbolinksNavMode.TO_MODAL -> {
                navigateToModalContext(rule)
            }
            TurbolinksNavMode.IN_CONTEXT -> {
                navigateWithinContext(rule)
            }
            TurbolinksNavMode.REFRESH -> {
                navigate(rule.currentLocation, TurbolinksVisitOptions())
            }
            TurbolinksNavMode.NONE -> {
                // Do nothing
            }
        }
    }

    fun clearBackStack(onCleared: () -> Unit = {}) {
        if (isAtStartDestination()) {
            onCleared()
            return
        }

        onNavigationVisit {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
            onCleared()
        }
    }

    private fun navigateWithinContext(rule: TurbolinksNavRule) {
        logEvent(
            "navigateWithinContext",
            "location" to rule.newLocation,
            "presentation" to rule.newPresentation
        )

        when (rule.newPresentation) {
            TurbolinksNavPresentation.POP -> onNavigationVisit {
                rule.controller.popBackStack()
            }
            TurbolinksNavPresentation.REPLACE -> onNavigationVisit {
                rule.controller.popBackStack()
                navigateToLocation(rule)
            }
            TurbolinksNavPresentation.PUSH -> onNavigationVisit {
                navigateToLocation(rule)
            }
            TurbolinksNavPresentation.REPLACE_ROOT -> onNavigationVisit {
                replaceRootLocation(rule)
            }
            TurbolinksNavPresentation.CLEAR_ALL -> onNavigationVisit {
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
            TurbolinksNavPresentation.REPLACE -> onNavigationVisit {
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
            val isDialog = fragment is DialogFragment
            if (isDialog) {
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
        navDestination.sessionViewModel.sendModalResult(
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
        navDestination.sessionViewModel.saveVisitOptions(rule.newVisitOptions)

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
        return navDestination.navHostForNavigation(location).navController
    }

    private fun isAtStartDestination(): Boolean {
        return currentController().previousBackStackEntry == null
    }

    private fun shouldNavigate(location: String): Boolean {
        val shouldNavigate = navDestination.shouldNavigateTo(location)

        logEvent(
            "shouldNavigateToLocation",
            "location" to location,
            "shouldNavigate" to shouldNavigate
        )
        return shouldNavigate
    }

    private fun navOptions(location: String): NavOptions {
        val properties = session.pathConfiguration.properties(location)

        return navDestination.getNavigationOptions(
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
