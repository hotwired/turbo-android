package com.basecamp.turbolinks

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksActivityDelegate(val activity: AppCompatActivity,
                                 var currentNavHostId: Int) {

    private val navHosts = mutableListOf<TurbolinksNavHost>()

    val currentDestination: TurbolinksDestination
        get() = currentFragment as TurbolinksDestination

    /*
     * Initialize the Activity with a BackPressedDispatcher that
     * properly handles Fragment navigation with the back button.
     */
    init {
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    fun registerNavHost(@IdRes navHostId: Int): TurbolinksNavHost {
        return findNavHost(navHostId).also {
            navHosts.add(it)
        }
    }

    fun navHost(@IdRes navHostId: Int): TurbolinksNavHost {
        return navHosts.firstOrNull { it.id == navHostId }
            ?: throw IllegalStateException("No registered TurbolinksNavHost found")
    }

    fun resetNavHosts() {
        navHosts.forEach { it.reset() }
    }

    fun resetSessions() {
        navHosts.forEach { it.session.reset() }
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
        get() = currentNavHostFragment.childFragmentManager.primaryNavigationFragment as Fragment

    private val currentNavHostFragment: TurbolinksNavHost
        get() = navHost(currentNavHostId)

    private fun findNavHost(@IdRes navHostId: Int): TurbolinksNavHost {
        return activity.supportFragmentManager.findFragmentById(navHostId) as? TurbolinksNavHost
            ?: throw IllegalStateException("No TurbolinksNavHost found with ID: $navHostId")
    }
}
