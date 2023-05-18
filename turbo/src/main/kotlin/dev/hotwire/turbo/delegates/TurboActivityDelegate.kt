package dev.hotwire.turbo.delegates

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.observers.TurboActivityObserver
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * Initializes the Activity for Turbo navigation and provides all the hooks for an
 * Activity to communicate with Turbo (and vice versa).
 *
 * @property activity The Activity to bind this delegate to.
 * @property currentNavHostFragmentId The resource ID of the [TurboSessionNavHostFragment]
 *  instance hosted in your Activity's layout resource.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TurboActivityDelegate(
    val activity: AppCompatActivity,
    currentNavHostFragmentId: Int
) {
    private val navHostFragments = mutableMapOf<Int, TurboSessionNavHostFragment>()

    private val onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
        override fun handleOnBackPressed() {
            navigateBack()
        }
    }

    /**
     * Gets or sets the currently active resource ID of the [TurboSessionNavHostFragment]
     *  instance hosted in your Activity's layout resource. If you use multiple nav host
     *  fragments in your app (such as for bottom tabs), you must update this whenever
     *  the currently active nav host fragment changes.
     */
    var currentNavHostFragmentId = currentNavHostFragmentId
        set(value) {
            field = value
            updateOnBackPressedCallback(currentSessionNavHostFragment.navController)
        }

    /**
     * Gets the Activity's currently active [TurboSessionNavHostFragment].
     */
    val currentSessionNavHostFragment: TurboSessionNavHostFragment
        get() = navHostFragment(currentNavHostFragmentId)

    /**
     * Gets the currently active Fragment destination hosted in the current
     * [TurboSessionNavHostFragment].
     */
    val currentNavDestination: TurboNavDestination?
        get() = currentFragment as TurboNavDestination?

    /**
     * Registers the provided nav host fragment and initializes the
     * Activity with a BackPressedDispatcher that properly handles Fragment
     * navigation with the back button.
     */
    init {
        registerNavHostFragment(currentNavHostFragmentId)
        activity.lifecycle.addObserver(TurboActivityObserver())
        activity.onBackPressedDispatcher.addCallback(activity, onBackPressedCallback)
    }

    /**
     * Provides the ability to register additional nav host fragments.
     *
     * @param navHostFragmentId
     * @return
     */
    fun registerNavHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return findNavHostFragment(navHostFragmentId).also {
            if (navHostFragments[navHostFragmentId] == null) {
                navHostFragments[navHostFragmentId] = it
                listenToDestinationChanges(it.navController)
            }
        }
    }

    /**
     * Finds the nav host fragment associated with the provided resource ID.
     *
     * @param navHostFragmentId
     * @return
     */
    fun navHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return requireNotNull(navHostFragments[navHostFragmentId]) {
            "No registered TurboSessionNavHostFragment found"
        }
    }

    /**
     * Resets the Turbo sessions associated with all registered nav host fragments.
     */
    fun resetSessions() {
        navHostFragments.forEach { it.value.session.reset() }
    }

    /**
     * Resets all registered nav host fragments via [TurboSessionNavHostFragment.reset].
     */
    fun resetNavHostFragments() {
        navHostFragments.forEach { it.value.reset() }
    }

    /**
     * Navigates to the specified location. The resulting destination and its presentation
     * will be determined using the path configuration rules.
     *
     * @param location The location to navigate to.
     * @param options Visit options to apply to the visit. (optional)
     * @param bundle Bundled arguments to pass to the destination. (optional)
     */
    fun navigate(
        location: String,
        options: TurboVisitOptions = TurboVisitOptions(),
        bundle: Bundle? = null
    ) {
        currentNavDestination?.navigate(location, options, bundle)
    }

    /**
     * Navigates up to the previous destination. See [NavController.navigateUp] for
     * more details.
     */
    fun navigateUp() {
        currentNavDestination?.navigateUp()
    }

    /**
     * Navigates back to the previous destination. See [NavController.popBackStack] for
     * more details.
     */
    fun navigateBack() {
        currentNavDestination?.navigateBack()
    }

    /**
     * Clears the navigation back stack to the start destination.
     */
    fun clearBackStack(onCleared: () -> Unit = {}) {
        currentNavDestination?.clearBackStack(onCleared)
    }

    /**
     * Refresh the current destination. See [TurboNavDestination.refresh] for
     * more details.
     */
    fun refresh(displayProgress: Boolean = true) {
        currentNavDestination?.refresh(displayProgress)
    }

    private fun listenToDestinationChanges(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, _, _ ->
            updateOnBackPressedCallback(controller)
        }
    }

    private fun updateOnBackPressedCallback(navController: NavController) {
        if (navController == currentSessionNavHostFragment.navController)  {
            onBackPressedCallback.isEnabled = navController.previousBackStackEntry != null
        }
    }

    private val currentFragment: Fragment?
        get() {
            return if (currentSessionNavHostFragment.isAdded && !currentSessionNavHostFragment.isDetached) {
                currentSessionNavHostFragment.childFragmentManager.primaryNavigationFragment
            } else {
                null
            }
        }

    private fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return activity.supportFragmentManager.findFragmentById(navHostFragmentId) as? TurboSessionNavHostFragment
            ?: throw IllegalStateException("No TurboSessionNavHostFragment found with ID: $navHostFragmentId")
    }
}
