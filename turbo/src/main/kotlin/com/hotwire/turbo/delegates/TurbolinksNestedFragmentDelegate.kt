package com.hotwire.turbo.delegates

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.hotwire.turbo.nav.TurbolinksNavDestination
import com.hotwire.turbo.session.TurbolinksSessionNavHostFragment
import com.hotwire.turbo.visit.TurbolinksVisitOptions

/**
 * A simplified delegate that can be used when only part of the view is delegated to Turbolinks
 * instead of the entire view being swapped. Example: a search screen with a search bar at the top
 * that doesn't navigate away, but search results load in a section of the view below the search bar.
 *
 * @property fragment The fragment to bind this delegate to.
 * @constructor
 *
 * @param navHostFragmentId
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksNestedFragmentDelegate(val fragment: Fragment, navHostFragmentId: Int) {
    val navHostFragment by lazy { findNavHostFragment(navHostFragmentId) }

    val currentNavDestination: TurbolinksNavDestination
        get() = currentFragment as TurbolinksNavDestination

    /**
     * Resets the nav host fragment via [TurbolinksSessionNavHostFragment.reset]
     *
     */
    fun resetNavHostFragment() {
        navHostFragment.reset()
    }

    /**
     * Resets the Turbolinks session associated with the nav host fragment.
     *
     */
    fun resetSession() {
        navHostFragment.session.reset()
    }

    /**
     * Navigates to the specified location using the current destination as the starting point.
     *
     * @param location The location to navigate to.
     * @param options Any options to apply to the visit.
     * @param bundle Any additional bundled data to pass to the navigation components.
     */
    fun navigate(
        location: String,
        options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
        bundle: Bundle? = null
    ) {
        currentNavDestination.navigate(location, options, bundle)
    }

    /**
     * Navigates up using the current destination as the starting point.
     *
     */
    fun navigateUp() {
        currentNavDestination.navigateUp()
    }

    /**
     * Navigates back using the current destination as the starting point.
     *
     */
    fun navigateBack() {
        currentNavDestination.navigateBack()
    }

    /**
     * Clears the nav back stack.
     *
     */
    fun clearBackStack() {
        currentNavDestination.clearBackStack()
    }

    private val currentFragment: Fragment
        get() = navHostFragment.childFragmentManager.primaryNavigationFragment as Fragment

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return fragment.childFragmentManager.findFragmentById(navHostFragmentId) as? TurbolinksSessionNavHostFragment
            ?: throw IllegalStateException("No TurbolinksNavHostFragment found with ID: $navHostFragmentId")
    }
}
