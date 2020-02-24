package com.basecamp.turbolinks

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import java.net.URI

class TurbolinksNavigator(private val destination: TurbolinksDestination) {
    private val fragment = destination.fragment
    private val session = destination.session
    private val router = destination.router

    var onNavigationVisit: (onNavigate: () -> Unit) -> Unit = { onReady ->
        destination.onBeforeNavigation()
        onReady()
    }

    fun navigateUp(): Boolean {
        onNavigationVisit {}
        return currentController().navigateUp()
    }

    fun navigateBack() {
        popBackStack()
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

        val pathProperties = currentPathConfiguration().properties(location)
        val currentContext = currentPresentationContext()
        val newContext = pathProperties.context
        val presentation = presentation(location, options, pathProperties)
        val navigationMode = navigationMode(currentContext, newContext, presentation)

        if (!shouldNavigate(location, pathProperties)) {
            return
        }

        logEvent("navigate", "location" to location,
            "options" to options, "currentContext" to currentContext,
            "newContext" to newContext, "presentation" to presentation)

        when (navigationMode) {
            NavigationMode.DISMISS_MODAL -> {
                dismissModalContextWithResult(location, options, pathProperties)
            }
            NavigationMode.TO_MODAL -> {
                navigateToModalContext(location, options, pathProperties, bundle, extras)
            }
            NavigationMode.IN_CONTEXT -> {
                navigateWithinContext(location, options, pathProperties, presentation, bundle, extras)
            }
        }
    }

    private fun navigateWithinContext(location: String,
                                      options: VisitOptions,
                                      properties: PathProperties,
                                      presentation: Presentation,
                                      bundle: Bundle?,
                                      extras: FragmentNavigator.Extras?) {

        logEvent("navigateWithinContext", "location" to location, "presentation" to presentation)
        val navBundle = bundle.withNavArguments(location, options, presentation)

        when (presentation) {
            Presentation.POP -> onNavigationVisit {
                currentController().popBackStack()
            }
            Presentation.REPLACE -> onNavigationVisit {
                currentController().popBackStack()
                navigateToLocation(location, properties, navBundle, extras)
            }
            Presentation.PUSH -> onNavigationVisit {
                navigateToLocation(location, properties, navBundle, extras)
            }
            Presentation.REPLACE_ROOT -> onNavigationVisit {
                replaceRootLocation(location, properties, navBundle)
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

    private fun navigateToModalContext(location: String,
                                       options: VisitOptions,
                                       properties: PathProperties,
                                       bundle: Bundle?,
                                       extras: FragmentNavigator.Extras?) {

        logEvent("navigateToModalContext", "location" to location)
        val navBundle = bundle.withNavArguments(location, options, Presentation.PUSH)

        onNavigationVisit {
            navigateToLocation(location, properties, navBundle, extras)
        }
    }

    private fun dismissModalContextWithResult(location: String, options: VisitOptions, properties: PathProperties) {
        logEvent("dismissModalContextWithResult",
            "location" to location,
            "uri" to properties.uri,
            "presentation" to properties.presentation
        )

        onNavigationVisit {
            val controller = currentController()
            val destination = controller.currentDestination

            if (destination == null) {
                logEvent("dismissModalContextWithResult", "error" to "No modal graph found")
                return@onNavigationVisit
            }

            sendModalResult(location, options, properties)
            controller.popBackStack(destination.id, true)
        }
    }

    private fun sendModalResult(location: String, options: VisitOptions, properties: PathProperties) {
        destination.sessionViewModel.sendModalResult(
            TurbolinksModalResult(
                location = location,
                options = options,
                shouldNavigate = properties.presentation != Presentation.NONE
            )
        )
    }

    private fun replaceRootLocation(location: String, properties: PathProperties, bundle: Bundle) {
        val controller = currentController()
        val destination = destinationFor(properties.uri)

        if (destination == null) {
            logEvent("replaceRootLocation", "error" to "No destination found")
            return
        }

        val navOptions = navOptions {
            popUpTo(destination.id) { inclusive = true }
        }

        logEvent("replaceRootLocation", "location" to location, "uri" to properties.uri)
        controller.navigate(destination.id, bundle, navOptions)
    }

    private fun navigateToLocation(location: String,
                                   properties: PathProperties,
                                   bundle: Bundle,
                                   extras: FragmentNavigator.Extras?) {

        val controller = currentController()
        val navOptions = navOptions(location, properties)

        destinationFor(properties.uri)?.let { destination ->
            logEvent("navigateToLocation", "location" to location, "uri" to properties.uri)
            controller.navigate(destination.id, bundle, navOptions, extras)
            return
        }

        logEvent("navigateToLocation", "location" to location,
            "warning" to "No destination found", "uri" to properties.uri)

        val fallbackUri = router.getFallbackDeepLinkUri(location)

        destinationFor(fallbackUri)?.let { destination ->
            logEvent("navigateToLocation", "location" to location, "fallbackUri" to fallbackUri)
            controller.navigate(destination.id, bundle, navOptions, extras)
            return
        }

        logEvent("navigateToLocation", "location" to location,
            "error" to "No fallback destination found", "uri" to fallbackUri)
    }

    private fun presentation(location: String, options: VisitOptions, properties: PathProperties): Presentation {
        val presentation = properties.presentation

        logEvent("presentation", "location" to location, "presentation" to presentation)

        if (presentation == Presentation.DEFAULT) {
            val locationIsRoot = locationsAreSame(location, session.rootLocation)
            val locationIsCurrent = locationsAreSame(location, currentLocation())
            val locationIsPrevious = locationsAreSame(location, previousLocation())
            val replace = options.action == VisitAction.REPLACE

            return when {
                locationIsRoot && locationIsCurrent -> Presentation.REPLACE_ROOT
                locationIsPrevious -> Presentation.POP
                locationIsRoot -> Presentation.REPLACE_ALL
                locationIsCurrent || replace -> Presentation.REPLACE
                else -> Presentation.PUSH
            }
        } else {
            return presentation
        }
    }

    private fun navigationMode(currentContext: PresentationContext,
                               newContext: PresentationContext,
                               presentation: Presentation): NavigationMode {

        val dismissModalContext = currentContext == PresentationContext.MODAL &&
                newContext == PresentationContext.DEFAULT &&
                presentation != Presentation.REPLACE_ROOT

        val navigateToModalContext = currentContext == PresentationContext.DEFAULT &&
                newContext == PresentationContext.MODAL &&
                presentation != Presentation.REPLACE_ROOT

        return when {
            dismissModalContext -> NavigationMode.DISMISS_MODAL
            navigateToModalContext -> NavigationMode.TO_MODAL
            else -> NavigationMode.IN_CONTEXT
        }
    }

    private fun currentController(): NavController {
        return fragment.findNavController()
    }

    private fun destinationFor(uri: Uri): NavDestination? {
        return currentController().graph.find { it.hasDeepLink(uri) }
    }

    private fun popBackStack() {
        onNavigationVisit {
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
        if (first == null || second == null) {
            return false
        }

        return URI(first).path == URI(second).path
    }

    private fun Bundle?.withNavArguments(location: String, options: VisitOptions, presentation: Presentation): Bundle {
        val previousLocation = when (presentation) {
            Presentation.PUSH -> currentLocation()
            else -> previousLocation()
        }

        val bundle = this ?: bundleOf()
        val navBundle = bundleOf(
            "location" to location,
            "previousLocation" to previousLocation,
            "visitOptions" to options.toJson(),
            "sessionName" to session.sessionName
        )

        return bundle.apply { putAll(navBundle) }
    }

    private fun shouldNavigate(location: String, properties: PathProperties): Boolean {
        val shouldNavigate = router.shouldNavigate(
            currentLocation = currentLocation(),
            newLocation = location,
            currentPathProperties = currentPathProperties(),
            newPathProperties = properties
        )

        logEvent("shouldNavigateToLocation", "location" to location, "shouldNavigate" to shouldNavigate)
        return shouldNavigate
    }

    private fun navOptions(location: String, properties: PathProperties): NavOptions {
        return router.getNavigationOptions(
            currentLocation = currentLocation(),
            newLocation = location,
            currentPathProperties = currentPathProperties(),
            newPathProperties = properties
        ) ?: navOptions {
            anim {
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
            }
        }
    }

    private fun currentLocation(): String {
        return checkNotNull(fragment.arguments?.getString("location"))
    }

    private fun previousLocation(): String? {
        return fragment.arguments?.getString("previousLocation")
    }

    private fun currentPathConfiguration(): PathConfiguration {
        return session.pathConfiguration
    }

    private fun currentPathProperties(): PathProperties {
        return currentPathConfiguration().properties(currentLocation())
    }

    private fun currentPresentationContext(): PresentationContext {
        return currentPathProperties().context
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to session.sessionName)
            add("fragment" to fragment.javaClass.simpleName)
        }
        logEvent(event, attributes)
    }
}
