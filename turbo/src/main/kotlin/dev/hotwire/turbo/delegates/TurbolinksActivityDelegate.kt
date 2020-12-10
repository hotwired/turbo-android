package dev.hotwire.turbo.delegates

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.hotwire.turbo.nav.TurbolinksNavDestination
import dev.hotwire.turbo.session.TurbolinksSessionNavHostFragment
import dev.hotwire.turbo.visit.TurbolinksVisitOptions

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

    /**
     * Provides the ability to register additional nav host fragments.
     *
     * @param navHostFragmentId
     * @return
     */
    fun registerNavHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return findNavHostFragment(navHostFragmentId).also {
            navHostFragments[navHostFragmentId] = it
        }
    }

    /**
     * Finds the nav host fragment associated with the provided ID.
     *
     * @param navHostFragmentId
     * @return
     */
    fun navHostFragment(@IdRes navHostFragmentId: Int): TurbolinksSessionNavHostFragment {
        return requireNotNull(navHostFragments[navHostFragmentId]) {
            "No registered TurbolinksNavHostFragment found"
        }
    }

    /**
     * Resets the Turbolinks sessions associated with each nav host fragment.
     *
     */
    fun resetSessions() {
        navHostFragments.forEach { it.value.session.reset() }
    }

    /**
     * Resets each nav host fragment via [TurbolinksSessionNavHostFragment.reset].
     *
     */
    fun resetNavHostFragments() {
        navHostFragments.forEach { it.value.reset() }
    }

    /**
     * Navigates to the specified location using the current destination as the starting point.
     *
     * @param location The location to navigate to.
     * @param options Any options to apply to the visit.
     * @param bundle Any additional bundled data to pass to the navigation components.
     */
    fun navigate(
        location: String,
        options: TurbolinksVisitOptions = TurbolinksVisitOptions(),
        bundle: Bundle? = null
    ) {
        currentNavDestination?.navigate(location, options, bundle)
    }

    /**
     * Navigates up using the current destination as the starting point.
     *
     */
    fun navigateUp() {
        currentNavDestination?.navigateUp()
    }

    /**
     * Navigates back using the current destination as the starting point.
     *
     */
    fun navigateBack() {
        currentNavDestination?.navigateBack()
    }

    /**
     * Clears the nav back stack.
     *
     */
    fun clearBackStack(onCleared: () -> Unit = {}) {
        currentNavDestination?.clearBackStack(onCleared)
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
