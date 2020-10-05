package com.basecamp.turbolinks.fragment

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.core.TurbolinksDestination
import com.basecamp.turbolinks.core.VisitOptions
import com.basecamp.turbolinks.nav.TurbolinksNavHostFragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksNestedFragmentDelegate(val fragment: Fragment, navHostFragmentId: Int) {
    val navHostFragment by lazy { findNavHostFragment(navHostFragmentId) }

    val currentDestination: TurbolinksDestination
        get() = currentFragment as TurbolinksDestination

    fun resetNavHostFragment() {
        navHostFragment.reset()
    }

    fun resetSession() {
        navHostFragment.session.reset()
    }

    fun navigate(location: String,
                 options: VisitOptions = VisitOptions(),
                 bundle: Bundle? = null) {
        currentDestination.navigate(location, options, bundle)
    }

    fun navigateUp() {
        currentDestination.navigateUp()
    }

    fun navigateBack() {
        currentDestination.navigateBack()
    }

    fun clearBackStack() {
        currentDestination.clearBackStack()
    }

    private val currentFragment: Fragment
        get() = navHostFragment.childFragmentManager.primaryNavigationFragment as Fragment

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksNavHostFragment {
        return fragment.childFragmentManager.findFragmentById(navHostFragmentId) as? TurbolinksNavHostFragment
            ?: throw IllegalStateException("No TurbolinksNavHostFragment found with ID: $navHostFragmentId")
    }
}
