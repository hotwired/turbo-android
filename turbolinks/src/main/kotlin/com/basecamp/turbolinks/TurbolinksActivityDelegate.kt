package com.basecamp.turbolinks

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksActivityDelegate(val activity: AppCompatActivity,
                                 var currentNavHostFragmentId: Int) {

    private val navHostFragments = mutableMapOf<Int, TurbolinksNavHostFragment>()

    val currentNavHostFragment: TurbolinksNavHostFragment
        get() = navHostFragment(currentNavHostFragmentId)

    val currentDestination: TurbolinksDestination?
        get() = currentFragment as TurbolinksDestination?

    /*
     * Initialize the Activity with a BackPressedDispatcher that
     * properly handles Fragment navigation with the back button.
     */
    init {
        registerNavHostFragment(currentNavHostFragmentId)
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    fun registerNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksNavHostFragment {
        return findNavHostFragment(navHostFragmentId).also {
            navHostFragments[navHostFragmentId] = it
        }
    }

    fun navHostFragment(@IdRes navHostFragmentId: Int): TurbolinksNavHostFragment {
        return requireNotNull(navHostFragments[navHostFragmentId]) {
            "No registered TurbolinksNavHostFragment found"
        }
    }

    fun resetNavHostFragments() {
        navHostFragments.forEach { it.value.reset() }
    }

    fun resetSessions() {
        navHostFragments.forEach { it.value.session.reset() }
    }

    fun navigate(location: String,
                 options: VisitOptions = VisitOptions(),
                 bundle: Bundle? = null) {
        currentDestination?.navigate(location, options, bundle)
    }

    fun navigateUp() {
        currentDestination?.navigateUp()
    }

    fun navigateBack() {
        currentDestination?.navigateBack()
    }

    fun clearBackStack() {
        currentDestination?.clearBackStack()
    }

    private val currentFragment: Fragment?
        get() {
            return if (currentNavHostFragment.isAdded && !currentNavHostFragment.isDetached) {
                currentNavHostFragment.childFragmentManager.primaryNavigationFragment
            } else {
                null
            }
        }

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksNavHostFragment {
        return activity.supportFragmentManager.findFragmentById(navHostFragmentId) as? TurbolinksNavHostFragment
            ?: throw IllegalStateException("No TurbolinksNavHostFragment found with ID: $navHostFragmentId")
    }
}
