package com.basecamp.turbolinks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

abstract class TurbolinksActivity : AppCompatActivity(), TurbolinksFragment.OnFragmentListener {
    final override fun onSupportNavigateUp(): Boolean {
        detachWebViewFromCurrentDestination()
        return currentController().navigateUp()
    }

    final override fun onBackPressed() {
        popBackStack()
    }

    final override fun navigate(location: String, action: String) {
        detachWebViewFromCurrentDestination {
            val bundle = Bundle().apply { putString("location", location) }
            val controller = currentController()
            val router = onProvideRouter()

            if (action == "replace") {
                controller.popBackStack()
            }

            router.getNavigationAction(location)?.let { actionId ->
                controller.navigate(actionId, bundle)
            }
        }
    }

    final override fun popBackStack() {
        detachWebViewFromCurrentDestination {
            if (!currentController().popBackStack()) {
                finish()
            }
        }
    }

    fun isAtStartDestination(): Boolean {
        val controller = currentController()
        return controller.graph.startDestination == controller.currentDestination?.id
    }

    fun clearBackStack() {
        if (isAtStartDestination()) return

        detachWebViewFromCurrentDestination {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
        }
    }

    /**
     * It's necessary to detach the shared WebView from a screen *before* it is hidden or exits and
     * the navigation animations run. The framework animator expects that the View hierarchy will
     * not change during the transition. Because the incoming screen will attach the WebView to the
     * new view hierarchy, it needs to already be detached from the previous screen.
     */
    private fun detachWebViewFromCurrentDestination(onDetached: () -> Unit = {}) {
        currentDestinationAction {
            when (it) {
                is TurbolinksFragment -> it.detachWebView(onDetached)
                else -> onDetached()
            }
        }
    }

    private fun currentDestinationAction(action: (Fragment) -> Unit) {
        onProvideCurrentDestination().let(action)
    }

    private fun currentController(): NavController {
        return onProvideCurrentDestination().findNavController()
    }
}
