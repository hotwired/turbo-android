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

        val pathProperties = currentPathConfiguration().properties(location)
        val currentContext = currentPresentationContext()
        val newContext = pathProperties.context
        val presentation = presentation(location, options, pathProperties)
        val navigationMode = navigationMode(currentContext, newContext, presentation)

        logEvent(
            "navigate", "location" to location,
            "options" to options, "currentContext" to currentContext,
            "newContext" to newContext, "presentation" to presentation
        )

        when (navigationMode) {
            NavigationMode.DISMISS_MODAL -> {
                dismissModalContextWithResult(location, options, pathProperties)
            }
            NavigationMode.TO_MODAL -> {
                navigateToModalContext(location, options, pathProperties, presentation, bundle, extras)
            }
            NavigationMode.IN_CONTEXT -> {
                when (presentation) {
                    Presentation.REFRESH -> {
                        // Refresh signals reloading the current destination
                        // url, ignoring the provided `location` url.
                        navigate(currentLocation(), VisitOptions())
                    }
                    else -> {
                        navigateWithinContext(location, options, pathProperties, presentation, bundle, extras)
                    }
                }
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
        val navBundle = bundle.withNavArguments(location, presentation)
        val controller = currentControllerForLocation(location)

        when (presentation) {
            Presentation.POP -> onNavigationVisit {
                controller.popBackStack()
            }
            Presentation.REPLACE -> onNavigationVisit {
                controller.popBackStack()
                navigateToLocation(location, options, properties, navBundle, extras)
            }
            Presentation.PUSH -> onNavigationVisit {
                navigateToLocation(location, options, properties, navBundle, extras)
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
                                       presentation: Presentation,
                                       bundle: Bundle?,
                                       extras: FragmentNavigator.Extras?) {

        logEvent("navigateToModalContext", "location" to location)
        val navBundle = bundle.withNavArguments(location, Presentation.PUSH)
        val controller = currentControllerForLocation(location)

        when (presentation) {
            Presentation.REPLACE -> onNavigationVisit {
                controller.popBackStack()
                navigateToLocation(location, options, properties, navBundle, extras)
            }
            else -> onNavigationVisit {
                navigateToLocation(location, options, properties, navBundle, extras)
            }
        }
    }

    private fun dismissModalContextWithResult(location: String, options: VisitOptions, properties: PathProperties) {
        logEvent("dismissModalContextWithResult",
            "location" to location,
            "uri" to properties.uri,
            "presentation" to properties.presentation
        )

        onNavigationVisit {
            val controller = currentControllerForLocation(location)
            val navDestination = checkNotNull(controller.currentDestination)

            sendModalResult(location, options, properties)
            controller.popBackStack(navDestination.id, true)
        }
    }

    private fun sendModalResult(location: String, options: VisitOptions, properties: PathProperties) {
        // Save the modal result with VisitOptions so it can be retrieved
        // by the previous destination when the backstack is popped.
        destination.sessionViewModel.sendModalResult(
            TurbolinksModalResult(
                location = location,
                options = options,
                shouldNavigate = properties.presentation != Presentation.NONE
            )
        )
    }

    private fun replaceRootLocation(location: String, properties: PathProperties, bundle: Bundle) {
        val controller = currentControllerForLocation(location)
        val destination = controller.destinationFor(properties.uri)

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
                                   options: VisitOptions,
                                   properties: PathProperties,
                                   bundle: Bundle,
                                   extras: FragmentNavigator.Extras?) {

        val controller = currentControllerForLocation(location)
        val navOptions = navOptions(location, properties)

        // Save the VisitOptions so it can be retrieved by the next
        // destination. When response.responseHTML is present it is
        // too large to save directly within the args bundle.
        destination.sessionViewModel.saveVisitOptions(options)

        controller.destinationFor(properties.uri)?.let { destination ->
            logEvent("navigateToLocation", "location" to location, "uri" to properties.uri)
            controller.navigate(destination.id, bundle, navOptions, extras)
            return
        }

        logEvent("navigateToLocation", "location" to location,
            "warning" to "No destination found", "uri" to properties.uri)

        controller.destinationFor(properties.fallbackUri)?.let { destination ->
            logEvent("navigateToLocation", "location" to location, "fallbackUri" to "${properties.fallbackUri}")
            controller.navigate(destination.id, bundle, navOptions, extras)
            return
        }

        logEvent("navigateToLocation", "location" to location,
            "error" to "No fallback destination found")
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

    private fun currentControllerForLocation(location: String): NavController {
        return destination.navHostForNavigation(location).navController
    }

    private fun NavController.destinationFor(uri: Uri?): NavDestination? {
        uri ?: return null
        return graph.find { it.hasDeepLink(uri) }
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

    private fun Bundle?.withNavArguments(location: String, presentation: Presentation): Bundle {
        val previousLocation = when (presentation) {
            Presentation.PUSH -> currentLocation()
            else -> previousLocation()
        }

        val bundle = this ?: bundleOf()
        val navBundle = bundleOf(
            "location" to location,
            "previousLocation" to previousLocation,
            "sessionName" to session.sessionName
        )

        return bundle.apply { putAll(navBundle) }
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

    private fun currentLocation(): String {
        val location = fragment.arguments?.getString("location")
        return checkNotNull(location)
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
