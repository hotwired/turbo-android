package com.basecamp.turbolinks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class TurbolinksActivityDelegate(activity: TurbolinksActivity) : TurbolinksActivity by activity {

    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String) {
        detachWebViewFromCurrentDestination(destinationIsFinishing = false) {
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

    override fun navigateUp(): Boolean {
        detachWebViewFromCurrentDestination(destinationIsFinishing = true)
        return currentController().navigateUp()
    }

    override fun navigateBack() {
        popBackStack()
    }

    override fun clearBackStack() {
        if (isAtStartDestination()) return

        detachWebViewFromCurrentDestination(destinationIsFinishing = true) {
            val controller = currentController()
            controller.popBackStack(controller.graph.startDestination, false)
        }
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun currentController(): NavController {
        return onProvideCurrentDestination().findNavController()
    }

    private fun popBackStack() {
        detachWebViewFromCurrentDestination(destinationIsFinishing = true) {
            if (!currentController().popBackStack()) {
                onRequestFinish()
            }
        }
    }

    private fun isAtStartDestination(): Boolean {
        val controller = currentController()
        return controller.graph.startDestination == controller.currentDestination?.id
    }

    /**
     * It's necessary to detach the shared WebView from a screen *before* it is hidden or exits and
     * the navigation animations run. The framework animator expects that the View hierarchy will
     * not change during the transition. Because the incoming screen will attach the WebView to the
     * new view hierarchy, it needs to already be detached from the previous screen.
     */
    private fun detachWebViewFromCurrentDestination(destinationIsFinishing: Boolean, onDetached: () -> Unit = {}) {
        currentDestinationAction {
            when (it) {
                is TurbolinksFragment -> it.onProvideObserver().detachWebView(destinationIsFinishing, onDetached)
                else -> onDetached()
            }
        }
    }

    private fun currentDestinationAction(action: (Fragment) -> Unit) {
        onProvideCurrentDestination().let(action)
    }
}
