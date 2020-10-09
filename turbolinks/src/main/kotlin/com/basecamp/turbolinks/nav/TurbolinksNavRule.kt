package com.basecamp.turbolinks.nav

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import com.basecamp.turbolinks.config.*
import com.basecamp.turbolinks.session.TurbolinksSessionModalResult
import com.basecamp.turbolinks.visit.TurbolinksVisitAction
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions
import java.net.URI

@Suppress("MemberVisibilityCanBePrivate")
internal class TurbolinksNavRule(
    location: String,
    visitOptions: TurbolinksVisitOptions,
    bundle: Bundle?,
    navOptions: NavOptions,
    extras: FragmentNavigator.Extras?,
    pathConfiguration: PathConfiguration,
    val controller: NavController
) {
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

    private fun newPresentation(): TurbolinksNavPresentation {
        // Use the custom presentation provided in the path configuration
        if (newProperties.presentation != TurbolinksNavPresentation.DEFAULT) {
            return newProperties.presentation
        }

        val locationIsCurrent = locationPathsAreEqual(newLocation, currentLocation)
        val locationIsPrevious = locationPathsAreEqual(newLocation, previousLocation)
        val replace = newVisitOptions.action == TurbolinksVisitAction.REPLACE

        return when {
            locationIsCurrent && isAtStartDestination -> TurbolinksNavPresentation.REPLACE_ROOT
            locationIsPrevious -> TurbolinksNavPresentation.POP
            locationIsCurrent || replace -> TurbolinksNavPresentation.REPLACE
            else -> TurbolinksNavPresentation.PUSH
        }
    }

    private fun newNavOptions(navOptions: NavOptions): NavOptions {
        // Use separate NavOptions if we need to pop up to the new root destination
        if (newPresentation == TurbolinksNavPresentation.REPLACE_ROOT && newDestination != null) {
            return navOptions {
                popUpTo(newDestination.id) { inclusive = true }
            }
        }

        return navOptions
    }

    private fun newNavigationMode(): TurbolinksNavMode {
        val presentationNone = newPresentation == TurbolinksNavPresentation.NONE
        val presentationRefresh = newPresentation == TurbolinksNavPresentation.REFRESH

        val dismissModalContext = currentPresentationContext == TurbolinksNavPresentationContext.MODAL &&
                newPresentationContext == TurbolinksNavPresentationContext.DEFAULT &&
                newPresentation != TurbolinksNavPresentation.REPLACE_ROOT

        val navigateToModalContext = currentPresentationContext == TurbolinksNavPresentationContext.DEFAULT &&
                newPresentationContext == TurbolinksNavPresentationContext.MODAL &&
                newPresentation != TurbolinksNavPresentation.REPLACE_ROOT

        return when {
            dismissModalContext -> TurbolinksNavMode.DISMISS_MODAL
            navigateToModalContext -> TurbolinksNavMode.TO_MODAL
            presentationRefresh -> TurbolinksNavMode.REFRESH
            presentationNone -> TurbolinksNavMode.NONE
            else -> TurbolinksNavMode.IN_CONTEXT
        }
    }

    private fun newModalResult(): TurbolinksSessionModalResult? {
        if (newNavigationMode != TurbolinksNavMode.DISMISS_MODAL) {
            return null
        }

        return TurbolinksSessionModalResult(
            location = newLocation,
            options = newVisitOptions,
            bundle = newBundle,
            shouldNavigate = newProperties.presentation != TurbolinksNavPresentation.NONE
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
