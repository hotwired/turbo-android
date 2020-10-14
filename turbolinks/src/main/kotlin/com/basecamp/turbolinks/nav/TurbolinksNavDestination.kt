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
 * The primary interface that a navigable Fragment must implement to provide the library with
 * the information it needs to properly navigate.
 *
 * @constructor Create empty Turbolinks nav destination
 */
interface TurbolinksNavDestination {
    /**
     * Convenience property that casts the this destination as a Fragment.
     */
    val fragment: Fragment
        get() = this as Fragment

    /**
     * Provides access to the Turbolinks Session nav host fragment associated with this destination.
     */
    val sessionNavHostFragment: TurbolinksSessionNavHostFragment
        get() = fragment.parentFragment as TurbolinksSessionNavHostFragment

    /**
     * Provides access to the location stored in the Fragment's arguments.
     */
    val location: String
        get() = requireNotNull(fragment.arguments?.location)

    /**
     * Provides access to the previous back stack entry's location from the nav controller.
     */
    val previousLocation: String?
        get() = navController()?.previousBackStackEntry?.arguments?.location

    /**
     * Provides access to the path configuration properties for the location associated with this
     * destination.
     */
    val pathProperties: TurbolinksPathConfigurationProperties
        get() = pathConfiguration.properties(location)

    /**
     * Provides access to the TurbolinksSession associated with this destination.
     */
    val session: TurbolinksSession
        get() = sessionNavHostFragment.session

    /**
     * Provides access to the TurbolinksSessionViewModel associated with this destination.
     */
    val sessionViewModel: TurbolinksSessionViewModel
        get() = delegate().sessionViewModel

    /**
     * Provides access to the TurbolinksFragmentViewModel associated with this destination.
     */
    val fragmentViewModel: TurbolinksFragmentViewModel
        get() = delegate().fragmentViewModel

    /**
     * Returns the fragment delegate provided by the implementing class.
     *
     * @return
     */
    fun delegate(): TurbolinksFragmentDelegate

    /**
     * Returns the [Toolbar] used for navigation by the given view.
     *
     * @return
     */
    fun toolbarForNavigation(): Toolbar?

    /**
     * Any actions that should be consistently executed before navigating (e.g., any state clean up).
     *
     */
    fun onBeforeNavigation()

    /**
     * Provides access to the [TurbolinksSessionNavHostFragment] used by this destination's session.
     *
     * @param newLocation The destination's new location.
     * @return
     */
    fun navHostForNavigation(newLocation: String): TurbolinksSessionNavHostFragment {
        return sessionNavHostFragment
    }

    /**
     * Implementing fragments can determine their own rules for when navigation should or shouldn't
     * execute (e.g., certains paths like mailto:'s may not be appropriate to send through the
     * normal navigation flow).
     *
     * @param newLocation
     * @return
     */
    fun shouldNavigateTo(newLocation: String): Boolean {
        return true
    }

    /**
     * Executes the navigation via the [TurbolinksNavigator].
     *
     * @param location The location to navigate to.
     * @param options The visit options to use to process the navigation.
     * @param bundle Any bundle arguments to pass along to the Android navigation components.
     * @param extras Any extras to pass along to the Android navigation components.
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
     * Provides a default set of navigation options (basic enter/exit animations) for the Android
     * Navigation components to use to execute a navigation event.
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
     * Tells the [TurbolinksNavigator] to navigate up.
     *
     */
    fun navigateUp() {
        navigator.navigateUp()
    }

    /**
     * Tells the [TurbolinksNavigator] to navigate back.
     *
     */
    fun navigateBack() {
        navigator.navigateBack()
    }

    /**
     * Tells the [TurbolinksNavigator] to clear the back stack. Will not clear if already at the
     * start destination for the nav host.
     *
     */
    fun clearBackStack() {
        navigator.clearBackStack()
    }

    /**
     * Finds the nav host fragment with the given ID.
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

    /**
     * Retrieve the nav controller indirectly from the parent NavHostFragment,
     * since it's only available when the fragment is attached to its parent
     *
     * @return
     */
    private fun navController(): NavController? {

        return fragment.parentFragment?.findNavController()
    }

    private fun FragmentManager.findNavHostFragment(navHostFragmentId: Int): TurbolinksSessionNavHostFragment? {
        return findFragmentById(navHostFragmentId) as? TurbolinksSessionNavHostFragment
    }
}
