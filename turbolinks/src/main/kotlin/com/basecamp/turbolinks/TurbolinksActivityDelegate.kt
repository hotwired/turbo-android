package com.basecamp.turbolinks

import android.app.Activity
import androidx.fragment.app.Fragment

class TurbolinksActivityDelegate(private val activity: Activity, private val turbolinksActivity: TurbolinksActivity) {
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
            router = turbolinksActivity.onProvideRouter()
        ) { _, onReady ->
            onReady()
        }
    }
}
