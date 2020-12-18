package dev.hotwire.turbo.delegates

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * A simplified delegate that can be used when a [TurboSessionNavHostFragment] is nested
 * within a Fragment. This can be useful when you want a portion of the screen to have
 * sub-navigation destinations within the current Fragment.
 *
 * Example: A search screen with a search bar at the top that stays fixed, but search
 * results load in a section of the view below the search bar.
 *
 * @property fragment The Fragment to bind this delegate to.
 * @param navHostFragmentId The resource ID of the [TurboSessionNavHostFragment]
 *  instance hosted in your Fragment's layout resource.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurboNestedFragmentDelegate(val fragment: Fragment, navHostFragmentId: Int) {
    val navHostFragment by lazy { findNavHostFragment(navHostFragmentId) }

    val currentNavDestination: TurboNavDestination
        get() = currentFragment as TurboNavDestination

    /**
     * Resets the nav host fragment via [TurboSessionNavHostFragment.reset]
     */
    fun resetNavHostFragment() {
        navHostFragment.reset()
    }

    /**
     * Resets the Turbo session associated with the nav host fragment.
     */
    fun resetSession() {
        navHostFragment.session.reset()
    }

    /**
     * Navigates to the specified location. The resulting destination and its presentation
     * will be determined using the path configuration rules.
     *
     * @param location The location to navigate to.
     * @param options Visit options to apply to the visit. (optional)
     * @param bundle Bundled arguments to pass to the destination. (optional)
     */
    fun navigate(
        location: String,
        options: TurboVisitOptions = TurboVisitOptions(),
        bundle: Bundle? = null
    ) {
        currentNavDestination.navigate(location, options, bundle)
    }

    /**
     * Navigates up to the previous destination. See [NavController.navigateUp] for
     * more details.
     */
    fun navigateUp() {
        currentNavDestination.navigateUp()
    }

    /**
     * Navigates back to the previous destination. See [NavController.popBackStack] for
     * more details.
     */
    fun navigateBack() {
        currentNavDestination.navigateBack()
    }

    /**
     * Clears the navigation back stack to the start destination.
     */
    fun clearBackStack() {
        currentNavDestination.clearBackStack()
    }

    private val currentFragment: Fragment
        get() = navHostFragment.childFragmentManager.primaryNavigationFragment as Fragment

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return fragment.childFragmentManager.findFragmentById(navHostFragmentId) as? TurboSessionNavHostFragment
            ?: throw IllegalStateException("No TurboSessionNavHostFragment found with ID: $navHostFragmentId")
    }
}
