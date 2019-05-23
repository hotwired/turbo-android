package com.basecamp.turbolinks

import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavArgument
import androidx.navigation.NavController

class TurbolinksActivityDelegate(private val turbolinksActivity: TurbolinksActivity) {
    /*
     * Initialize the Activity with a BackPressedDispatcher that
     * properly handles Fragment navigation with the back button.
     */
    fun onCreate(activity: FragmentActivity) {
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    /*
     * Dynamically set the controller graph and start destination,
     * so we can use a simplified navigation graph.
     */
    fun startControllerGraph(controller: NavController, startLocation: String,
                             navGraph: Int, startDestination: Int) {

        val location = NavArgument.Builder()
            .setDefaultValue(startLocation)
            .build()

        controller.graph = controller.navInflater.inflate(navGraph).apply {
            this.addArgument("location", location)
            this.startDestination = startDestination
        }
    }

    fun navigate(location: String, action: String = "advance"): Boolean {
        return when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.navigate(location, action)
            else -> navigator(destination).navigate(location, action)
        }
    }

    fun navigateUp(): Boolean {
        return when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.navigateUp()
            else -> navigator(destination).navigateUp()
        }
    }

    fun navigateBack() {
        when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.navigateBack()
            else -> navigator(destination).navigateBack()
        }
    }

    fun clearBackStack() {
        when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.clearBackStack()
            else -> navigator(destination).clearBackStack()
        }
    }

    fun currentDestination(): Fragment {
        return turbolinksActivity.onProvideCurrentNavHostFragment().childFragmentManager.primaryNavigationFragment ?:
            throw IllegalStateException("No current destination found in NavHostFragment")
    }

    private fun navigator(fragment: Fragment): TurbolinksNavigator {
        return TurbolinksNavigator(
            fragment = fragment,
            session = turbolinksActivity.onProvideSession(fragment),
            router = turbolinksActivity.onProvideRouter())
    }
}
