package com.basecamp.turbolinks.delegates

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.nav.TurbolinksNavDestination
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

/**
 * Provides all the hooks for an activity to communicate with Turbolinks (and vice versa).
 *
 * @property activity The activity to bind this delegate to.
 * @property currentNavHostFragmentId The resource ID of the view bound to the nav host fragment.
 * @constructor Create empty Turbolinks activity delegate
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurbolinksActivityDelegate(
    val activity: AppCompatActivity,
    var currentNavHostFragmentId: Int
) {

    private val navHostFragments = mutableMapOf<Int, TurbolinksSessionNavHostFragment>()

    /**
     * A reference to the session's nav host fragment.
     */
    val currentSessionNavHostFragment: TurbolinksSessionNavHostFragment
        get() = navHostFragment(currentNavHostFragmentId)

    /**
     * A reference to the current destination.
     */
    val currentNavDestination: TurbolinksNavDestination?
        get() = currentFragment as TurbolinksNavDestination?

    /**
     * Initializes the Activity with a BackPressedDispatcher that properly handles Fragment
     * navigation with the back button.
     */
    init {
        registerNavHostFragment(currentNavHostFragmentId)
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    internal fun registerNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return findNavHostFragment(navHostFragmentId).also {
            navHostFragments[navHostFragmentId] = it
        }
    }

    internal fun navHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return requireNotNull(navHostFragments[navHostFragmentId]) {
            "No registered TurbolinksNavHostFragment found"
        }
    }

    /**
     * Resets each nav host fragment via [TurbolinksSessionNavHostFragment.reset].
     *
     */
    fun resetNavHostFragments() {
        navHostFragments.forEach { it.value.reset() }
    }

    /**
     * Navigate
     *
     * @param location
     * @param options
     * @param bundle
     */
    fun navigate(
        location: String,
        options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
        bundle: Bundle? = null
    ) {
        currentNavDestination?.navigate(location, options, bundle)
    }

    /**
     * Navigate up
     *
     */
    fun navigateUp() {
        currentNavDestination?.navigateUp()
    }

    /**
     * Navigate back
     *
     */
    fun navigateBack() {
        currentNavDestination?.navigateBack()
    }

    /**
     * Clear back stack
     *
     */
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
