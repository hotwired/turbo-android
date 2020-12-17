package dev.hotwire.turbo.nav

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
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.config.TurboPathConfigurationProperties
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.fragments.TurboFragmentViewModel
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import dev.hotwire.turbo.session.TurboSessionViewModel
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * The primary interface that a navigable Fragment must implement to provide the library with
 * the information it needs to properly navigate.
 *
 * @constructor Create empty Turbo nav destination
 */
interface TurboNavDestination {
    /**
     * Convenience property that casts the this destination as a Fragment.
     */
    val fragment: Fragment
        get() = this as Fragment

    /**
     * Provides access to the Turbo Session nav host fragment associated with this destination.
     */
    val sessionNavHostFragment: TurboSessionNavHostFragment
        get() = fragment.parentFragment as TurboSessionNavHostFragment

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
    val pathProperties: TurboPathConfigurationProperties
        get() = pathConfiguration.properties(location)

    /**
     * Provides access to the TurboSession associated with this destination.
     */
    val session: TurboSession
        get() = sessionNavHostFragment.session

    /**
     * Provides access to the TurboFragmentViewModel associated with this destination.
     */
    val fragmentViewModel: TurboFragmentViewModel
        get() = delegate().fragmentViewModel

    /**
     * Returns the fragment delegate provided by the implementing class.
     *
     * @return
     */
    fun delegate(): TurboFragmentDelegate

    /**
     * Returns the [Toolbar] used for navigation by the given view.
     *
     * @return
     */
    fun toolbarForNavigation(): Toolbar?

    /**
     * Specifies whether title changes should be automatically observed and update
     * the title in the Toolbar provided from toolbarForNavigation(), if available.
     * Default is true.
     */
    fun shouldObserveTitleChanges(): Boolean {
        return true
    }

    /**
     * Any actions that should be consistently executed before navigating (e.g., any state clean up).
     *
     */
    fun onBeforeNavigation()

    /**
     * Provides access to the [TurboSessionNavHostFragment] used by this destination's session.
     *
     * @param newLocation The destination's new location.
     * @return
     */
    fun navHostForNavigation(newLocation: String): TurboSessionNavHostFragment {
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
     * Executes the navigation via the [TurboNavigator].
     *
     * @param location The location to navigate to.
     * @param options The visit options to use to process the navigation.
     * @param bundle Any bundle arguments to pass along to the Android navigation components.
     * @param extras Any extras to pass along to the Android navigation components.
     */
    fun navigate(
        location: String,
        options: TurboVisitOptions = TurboVisitOptions(),
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
        newPathProperties: TurboPathConfigurationProperties
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
     * Tells the [TurboNavigator] to navigate up.
     *
     */
    fun navigateUp() {
        navigator.navigateUp()
    }

    /**
     * Tells the [TurboNavigator] to navigate back.
     *
     */
    fun navigateBack() {
        navigator.navigateBack()
    }

    /**
     * Tells the [TurboNavigator] to clear the back stack. Will not clear if already at the
     * start destination for the nav host.
     *
     */
    fun clearBackStack(onCleared: () -> Unit = {}) {
        navigator.clearBackStack(onCleared)
    }

    /**
     * Finds the nav host fragment with the given ID.
     *
     * @param navHostFragmentId
     * @return
     */
    fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return fragment.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.parentFragment?.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.requireActivity().supportFragmentManager.findNavHostFragment(navHostFragmentId)
            ?: throw IllegalStateException("No TurboSessionNavHostFragment found with ID: $navHostFragmentId")
    }

    private val Bundle.location
        get() = getString("location")

    private val navigator: TurboNavigator
        get() = delegate().navigator

    private val pathConfiguration: TurboPathConfiguration
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

    private fun FragmentManager.findNavHostFragment(navHostFragmentId: Int): TurboSessionNavHostFragment? {
        return findFragmentById(navHostFragmentId) as? TurboSessionNavHostFragment
    }
}
