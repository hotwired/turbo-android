package com.basecamp.turbolinks.fragment

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.nav.TurbolinksNavDestination
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksNestedFragmentDelegate(val fragment: Fragment, navHostFragmentId: Int) {
    val navHostFragment by lazy { findNavHostFragment(navHostFragmentId) }

    val currentNavDestination: TurbolinksNavDestination
        get() = currentFragment as TurbolinksNavDestination

    fun resetNavHostFragment() {
        navHostFragment.reset()
    }

    fun resetSession() {
        navHostFragment.session.reset()
    }

    fun navigate(location: String,
                 options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
                 bundle: Bundle? = null) {
        currentNavDestination.navigate(location, options, bundle)
    }

    fun navigateUp() {
        currentNavDestination.navigateUp()
    }

    fun navigateBack() {
        currentNavDestination.navigateBack()
    }

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
