package com.basecamp.turbolinks

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class TurbolinksActivityDelegate(private val activity: Activity, private val turbolinksActivity: TurbolinksActivity) {
    fun navigateUp(): Boolean {
        return when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.navigateUp()
            else -> destination.findNavController().navigateUp()
        }
    }

    fun navigateBack() {
        when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.navigateBack()
            else -> popBackStack(destination.findNavController())
        }
    }

    fun clearBackStack() {
        when (val destination = currentDestination()) {
            is TurbolinksFragment -> destination.clearBackStack()
            else -> {
                val controller = destination.findNavController()
                controller.popBackStack(controller.graph.startDestination, false)
            }
        }
    }

    fun currentDestination(): Fragment {
        return turbolinksActivity.onProvideCurrentNavHostFragment().childFragmentManager.primaryNavigationFragment ?:
            throw IllegalStateException("No current destination found in NavHostFragment")
    }

    private fun popBackStack(controller: NavController) {
        if (!controller.popBackStack()) {
            activity.finish()
        }
    }
}
