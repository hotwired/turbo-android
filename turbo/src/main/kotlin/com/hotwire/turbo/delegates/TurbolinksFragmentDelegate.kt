package com.hotwire.turbo.delegates

import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.hotwire.turbo.fragments.TurbolinksFragmentViewModel
import com.hotwire.turbo.nav.TurbolinksNavDestination
import com.hotwire.turbo.nav.TurbolinksNavigator
import com.hotwire.turbo.session.TurbolinksSessionModalResult
import com.hotwire.turbo.session.TurbolinksSessionViewModel
import com.hotwire.turbo.util.logEvent

/**
 * Provides all the hooks for a fragment to communicate with Turbolinks (and vice versa).
 *
 * @property navDestination The destination to bind to this delegate instance.
 * @constructor Create empty Turbolinks fragment delegate
 */
class TurbolinksFragmentDelegate(private val navDestination: TurbolinksNavDestination) {
    private val fragment = navDestination.fragment
    private val location = navDestination.location
    private val sessionName = navDestination.sessionNavHostFragment.sessionName

    internal val sessionViewModel = TurbolinksSessionViewModel.get(sessionName, fragment.requireActivity())
    internal val fragmentViewModel = TurbolinksFragmentViewModel.get(location, fragment)

    internal lateinit var navigator: TurbolinksNavigator

    /**
     * Should be called by the implementing Fragment during [androidx.fragment.app.Fragment.onActivityCreated].
     * Executes initial Turbolinks setup, including instantiating a [TurbolinksNavigator] and setting up toolbar clicks.
     *
     */
    fun onActivityCreated() {
        navigator = TurbolinksNavigator(navDestination)

        initToolbar()
        logEvent("fragment.onActivityCreated", "location" to location)
    }

    /**
     * Should be called by the implementing Fragment during [androidx.fragment.app.Fragment.onStart].
     * Currently doesn't do anything.
     *
     */
    fun onStart() {
        logEvent("fragment.onStart", "location" to location)
    }

    /**
     * Should be called by the implementing Fragment during [androidx.fragment.app.Fragment.onStart].
     * Currently doesn't do anything.
     *
     */
    fun onStop() {
        logEvent("fragment.onStop", "location" to location)
    }

    /**
     * Provides a hook to Turbolinks when the fragment has been started again after a dialog has
     * been dismissed/canceled and no result is passed back. Currently doesn't do anything.
     *
     */
    fun onStartAfterDialogCancel() {
        logEvent("fragment.onStartAfterDialogCancel", "location" to location)
    }

    /**
     * Provides a hook to Turbolinks when a fragment has been started again after receiving a
     * modal result. Will navigate if the result indicates it should.
     *
     * @param result
     */
    fun onStartAfterModalResult(result: TurbolinksSessionModalResult) {
        logEvent("fragment.onStartAfterModalResult", "location" to result.location, "options" to result.options)
        if (result.shouldNavigate) {
            navigator.navigate(result.location, result.options, result.bundle)
        }
    }

    /**
     * Provides a hook to Turbolinks when the dialog has been canceled. If there is a modal
     * result, an event will be created in [TurbolinksSessionViewModel] that can be observed.
     *
     */
    fun onDialogCancel() {
        logEvent("fragment.onDialogCancel", "location" to location)
        if (!sessionViewModel.modalResultExists) {
            sessionViewModel.sendDialogResult()
        }
    }

    fun onDialogDismiss() {
        logEvent("fragment.onDialogDismiss", "location" to location)
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun initToolbar() {
        navDestination.toolbarForNavigation()?.let {
            NavigationUI.setupWithNavController(it, fragment.findNavController())
            it.setNavigationOnClickListener {
                when (fragment) {
                    is DialogFragment -> fragment.requireDialog().cancel()
                    else -> navDestination.navigateUp()
                }
            }
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to sessionName)
            add("fragment" to fragment.javaClass.simpleName)
        }
        logEvent(event, attributes)
    }
}
