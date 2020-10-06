package com.basecamp.turbolinks.nav

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import com.basecamp.turbolinks.config.*
import com.basecamp.turbolinks.core.TurbolinksModalResult
import com.basecamp.turbolinks.core.VisitAction
import com.basecamp.turbolinks.core.VisitOptions
import java.net.URI

@Suppress("MemberVisibilityCanBePrivate")
class TurbolinksNavigationRule(
    location: String,
    visitOptions: VisitOptions,
    bundle: Bundle?,
    navOptions: NavOptions,
    extras: FragmentNavigator.Extras?,
    pathConfiguration: PathConfiguration,
    val controller: NavController
) {
    enum class PresentationContext {
        DEFAULT, MODAL
    }

    enum class Presentation {
        DEFAULT, PUSH, POP, REPLACE, REPLACE_ALL, REPLACE_ROOT, REFRESH, NONE
    }

    enum class NavigationMode {
        IN_CONTEXT, TO_MODAL, DISMISS_MODAL, REFRESH, NONE
    }

    // Current destination
    val previousLocation = controller.previousBackStackEntry.location
    val currentLocation = checkNotNull(controller.currentBackStackEntry.location)
    val currentProperties = pathConfiguration.properties(currentLocation)
    val currentPresentationContext = currentProperties.context
    val currentDestination = checkNotNull(controller.currentDestination)
    val isAtStartDestination = controller.previousBackStackEntry == null

    // New destination
    val newLocation = location
    val newVisitOptions = visitOptions
    val newBundle = bundle.withNavArguments()
    val newExtras = extras
    val newProperties = pathConfiguration.properties(newLocation)
    val newPresentationContext = newProperties.context
    val newPresentation = newPresentation()
    val newNavigationMode = newNavigationMode()
    val newModalResult = newModalResult()
    val newDestinationUri = newProperties.uri
    val newFallbackUri = newProperties.fallbackUri
    val newDestination = controller.destinationFor(newDestinationUri)
    val newFallbackDestination = controller.destinationFor(newFallbackUri)
    val newNavOptions = newNavOptions(navOptions)

    private fun newPresentation(): Presentation {
        // Use the custom presentation provided in the path configuration
        if (newProperties.presentation != Presentation.DEFAULT) {
            return newProperties.presentation
        }

        val locationIsCurrent = locationPathsAreEqual(newLocation, currentLocation)
        val locationIsPrevious = locationPathsAreEqual(newLocation, previousLocation)
        val replace = newVisitOptions.action == VisitAction.REPLACE

        return when {
            locationIsCurrent && isAtStartDestination -> Presentation.REPLACE_ROOT
            locationIsPrevious -> Presentation.POP
            locationIsCurrent || replace -> Presentation.REPLACE
            else -> Presentation.PUSH
        }
    }

    private fun newNavOptions(navOptions: NavOptions): NavOptions {
        // Use separate NavOptions if we need to pop up to the new root destination
        if (newPresentation == Presentation.REPLACE_ROOT && newDestination != null) {
            return navOptions {
                popUpTo(newDestination.id) { inclusive = true }
            }
        }

        return navOptions
    }

    private fun newNavigationMode(): NavigationMode {
        val presentationNone = newPresentation == Presentation.NONE
        val presentationRefresh = newPresentation == Presentation.REFRESH

        val dismissModalContext = currentPresentationContext == PresentationContext.MODAL &&
                newPresentationContext == PresentationContext.DEFAULT &&
                newPresentation != Presentation.REPLACE_ROOT

        val navigateToModalContext = currentPresentationContext == PresentationContext.DEFAULT &&
                newPresentationContext == PresentationContext.MODAL &&
                newPresentation != Presentation.REPLACE_ROOT

        return when {
            dismissModalContext -> NavigationMode.DISMISS_MODAL
            navigateToModalContext -> NavigationMode.TO_MODAL
            presentationRefresh -> NavigationMode.REFRESH
            presentationNone -> NavigationMode.NONE
            else -> NavigationMode.IN_CONTEXT
        }
    }

    private fun newModalResult(): TurbolinksModalResult? {
        if (newNavigationMode != NavigationMode.DISMISS_MODAL) {
            return null
        }

        return TurbolinksModalResult(
            location = newLocation,
            options = newVisitOptions,
            bundle = newBundle,
            shouldNavigate = newProperties.presentation != Presentation.NONE
        )
    }

    private fun NavController.destinationFor(uri: Uri?): NavDestination? {
        uri ?: return null
        return graph.find { it.hasDeepLink(uri) }
    }

    private fun Bundle?.withNavArguments(): Bundle {
        val bundle = this ?: bundleOf()
        return bundle.apply { putString("location", newLocation) }
    }

    private val NavBackStackEntry?.location: String?
        get() = this?.arguments?.getString("location")

    private fun locationPathsAreEqual(first: String?, second: String?): Boolean {
        if (first == null || second == null) {
            return false
        }

        return URI(first).path == URI(second).path
    }
}
