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
import com.basecamp.turbolinks.config.PathConfiguration
import com.basecamp.turbolinks.config.PathConfigurationSettings
import com.basecamp.turbolinks.config.PathProperties
import com.basecamp.turbolinks.delegates.TurbolinksFragmentDelegate
import com.basecamp.turbolinks.fragments.TurbolinksFragmentViewModel
import com.basecamp.turbolinks.session.TurbolinksSession
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment
import com.basecamp.turbolinks.session.TurbolinksSessionViewModel
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

interface TurbolinksNavDestination {
    val fragment: Fragment
        get() = this as Fragment

    val sessionNavHostFragment: TurbolinksSessionNavHostFragment
        get() = fragment.parentFragment as TurbolinksSessionNavHostFragment

    val location: String
        get() = requireNotNull(fragment.arguments?.location)

    val previousLocation: String?
        get() = navController()?.previousBackStackEntry?.arguments?.location

    val pathConfiguration: PathConfiguration
        get() = session.pathConfiguration

    val pathConfigurationSettings: PathConfigurationSettings
        get() = pathConfiguration.settings

    val pathProperties: PathProperties
        get() = pathConfiguration.properties(location)

    val session: TurbolinksSession
        get() = sessionNavHostFragment.session

    val sessionViewModel: TurbolinksSessionViewModel
        get() = delegate().sessionViewModel

    val pageViewModel: TurbolinksFragmentViewModel
        get() = delegate().pageViewModel

    fun delegate(): TurbolinksFragmentDelegate

    fun toolbarForNavigation(): Toolbar?

    fun onBeforeNavigation()

    fun navHostForNavigation(newLocation: String): TurbolinksSessionNavHostFragment {
        return sessionNavHostFragment
    }

    fun shouldNavigateTo(newLocation: String): Boolean {
        return true
    }

    fun navigate(
        location: String,
        options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
        bundle: Bundle? = null,
        extras: FragmentNavigator.Extras? = null
    ) {
        navigator.navigate(location, options, bundle, extras)
    }

    fun getNavigationOptions(
        newLocation: String,
        newPathProperties: PathProperties
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

    fun navigateUp() {
        navigator.navigateUp()
    }

    fun navigateBack() {
        navigator.navigateBack()
    }

    fun clearBackStack() {
        navigator.clearBackStack()
    }

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

    private fun navController(): NavController? {
        // Retrieve the nav controller indirectly from the parent NavHostFragment,
        // since it's only available when the fragment is attached to its parent
        return fragment.parentFragment?.findNavController()
    }

    private fun FragmentManager.findNavHostFragment(navHostFragmentId: Int): TurbolinksSessionNavHostFragment? {
        return findFragmentById(navHostFragmentId) as? TurbolinksSessionNavHostFragment
    }
}
