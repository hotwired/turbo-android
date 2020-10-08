package com.basecamp.turbolinks.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.nav.TurbolinksNavDestination
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksActivityDelegate(val activity: AppCompatActivity,
                                 var currentNavHostFragmentId: Int) {

    private val navHostFragments = mutableMapOf<Int, TurbolinksSessionNavHostFragment>()

    val currentSessionNavHostFragment: TurbolinksSessionNavHostFragment
        get() = navHostFragment(currentNavHostFragmentId)

    val currentNavDestination: TurbolinksNavDestination?
        get() = currentFragment as TurbolinksNavDestination?

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

    fun registerNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return findNavHostFragment(navHostFragmentId).also {
            navHostFragments[navHostFragmentId] = it
        }
    }

    fun navHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
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
                 options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
                 bundle: Bundle? = null) {
        currentNavDestination?.navigate(location, options, bundle)
    }

    fun navigateUp() {
        currentNavDestination?.navigateUp()
    }

    fun navigateBack() {
        currentNavDestination?.navigateBack()
    }

    fun clearBackStack() {
        currentNavDestination?.clearBackStack()
    }

    private val currentFragment: Fragment?
        get() {
            return if (currentSessionNavHostFragment.isAdded && !currentSessionNavHostFragment.isDetached) {
                currentSessionNavHostFragment.childFragmentManager.primaryNavigationFragment
            } else {
                null
            }
        }

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return activity.supportFragmentManager.findFragmentById(navHostFragmentId) as? TurbolinksSessionNavHostFragment
            ?: throw IllegalStateException("No TurbolinksNavHostFragment found with ID: $navHostFragmentId")
    }
}
