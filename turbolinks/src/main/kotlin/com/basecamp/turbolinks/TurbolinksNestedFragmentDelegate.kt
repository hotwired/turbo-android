package com.basecamp.turbolinks

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksNestedFragmentDelegate(val fragment: Fragment, navHostId: Int) {
    val navHost by lazy { findNavHost(navHostId) }

    val currentDestination: TurbolinksDestination
        get() = currentFragment as TurbolinksDestination

    fun resetNavHost() {
        navHost.reset()
    }

    fun resetSession() {
        navHost.session.reset()
    }

    fun navigate(location: String,
                 options: VisitOptions = VisitOptions(),
                 bundle: Bundle? = null) {
        currentDestination.navigate(location, options, bundle)
    }

    fun navigateUp(): Boolean {
        return currentDestination.navigateUp()
    }

    fun navigateBack() {
        currentDestination.navigateBack()
    }

    fun clearBackStack() {
        currentDestination.clearBackStack()
    }

    private val currentFragment: Fragment
        get() = navHost.childFragmentManager.primaryNavigationFragment as Fragment

    private fun findNavHost(@IdRes navHostId: Int): TurbolinksNavHost {
        return fragment.childFragmentManager.findFragmentById(navHostId) as? TurbolinksNavHost
            ?: throw IllegalStateException("No TurbolinksNavHost found with ID: $navHostId")
    }
}
