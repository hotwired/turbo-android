package com.basecamp.turbolinks.nav

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.basecamp.turbolinks.R
import com.basecamp.turbolinks.config.TurbolinksPathConfiguration
import com.basecamp.turbolinks.config.TurbolinksPathConfigurationProperties
import com.basecamp.turbolinks.delegates.TurbolinksFragmentDelegate
import com.basecamp.turbolinks.fragments.TurbolinksFragmentViewModel
import com.basecamp.turbolinks.session.TurbolinksSession
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment
import com.basecamp.turbolinks.session.TurbolinksSessionViewModel
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

/**
 * The primary interface that every navigable Fragment must implement to provide the library with
 * the information it needs to properly navigate.
 *
 * @constructor Create empty Turbolinks nav destination
 */
interface TurbolinksNavDestination {
    /**
     * Convenience property to cast the implementing class as a Fragment.
     */
    val fragment: Fragment
        get() = this as Fragment

    /**
     * Convenience property to access the Turbolinks Session nav host fragment associated with this
     * destination.
     */
    val sessionNavHostFragment: TurbolinksSessionNavHostFragment
        get() = fragment.parentFragment as TurbolinksSessionNavHostFragment

    /**
     * Convenience property to access the location stored in the Fragment's arguments.
     */
    val location: String
        get() = requireNotNull(fragment.arguments?.location)

    /**
     * Convenience property to access the previous back stack entry's location from the nav controller.
     */
    val previousLocation: String?
        get() = navController()?.previousBackStackEntry?.arguments?.location

    val pathProperties: TurbolinksPathConfigurationProperties
        get() = pathConfiguration.properties(location)

    val session: TurbolinksSession
        get() = sessionNavHostFragment.session

    val sessionViewModel: TurbolinksSessionViewModel
        get() = delegate().sessionViewModel

    val pageViewModel: TurbolinksFragmentViewModel
        get() = delegate().pageViewModel

    /**
     * Delegate
     *
     * @return
     */
    fun delegate(): TurbolinksFragmentDelegate

    /**
     * Toolbar for navigation
     *
     * @return
     */
    fun toolbarForNavigation(): Toolbar?

    /**
     * On before navigation
     *
     */
    fun onBeforeNavigation()

    /**
     * Nav host for navigation
     *
     * @param newLocation
     * @return
     */
    fun navHostForNavigation(newLocation: String): TurbolinksSessionNavHostFragment {
        return sessionNavHostFragment
    }

    /**
     * Should navigate to
     *
     * @param newLocation
     * @return
     */
    fun shouldNavigateTo(newLocation: String): Boolean {
        return true
    }

    /**
     * Navigate
     *
     * @param location
     * @param options
     * @param bundle
     * @param extras
     */
    fun navigate(
        location: String,
        options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
        bundle: Bundle? = null,
        extras: FragmentNavigator.Extras? = null
    ) {
        navigator.navigate(location, options, bundle, extras)
    }

    /**
     * Get navigation options
     *
     * @param newLocation
     * @param newPathProperties
     * @return
     */
    fun getNavigationOptions(
        newLocation: String,
        newPathProperties: TurbolinksPathConfigurationProperties
    ): NavOptions {
        return navOptions {
            anim {
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
            }
        }
    }

    /**
     * Navigate up
     *
     */
    fun navigateUp() {
        navigator.navigateUp()
    }

    /**
     * Navigate back
     *
     */
    fun navigateBack() {
        navigator.navigateBack()
    }

    /**
     * Clear back stack
     *
     */
    fun clearBackStack() {
        navigator.clearBackStack()
    }

    /**
     * Find nav host fragment
     *
     * @param navHostFragmentId
     * @return
     */
    fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return fragment.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.parentFragment?.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.requireActivity().supportFragmentManager.findNavHostFragment(navHostFragmentId)
            ?: throw IllegalStateException("No TurbolinksNavHostFragment found with ID: $navHostFragmentId")
    }

    private val Bundle.location
        get() = getString("location")

    private val navigator: TurbolinksNavigator
        get() = delegate().navigator

    private val pathConfiguration: TurbolinksPathConfiguration
        get() = session.pathConfiguration

    private fun navController(): NavController? {
        // Retrieve the nav controller indirectly from the parent NavHostFragment,
        // since it's only available when the fragment is attached to its parent
        return fragment.parentFragment?.findNavController()
    }

    private fun FragmentManager.findNavHostFragment(navHostFragmentId: Int): TurbolinksSessionNavHostFragment? {
        return findFragmentById(navHostFragmentId) as? TurbolinksSessionNavHostFragment
    }
}
