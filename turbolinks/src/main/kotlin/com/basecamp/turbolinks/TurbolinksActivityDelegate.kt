package com.basecamp.turbolinks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.TurbolinksPresentation.MODAL
import com.basecamp.turbolinks.TurbolinksPresentation.NORMAL

class TurbolinksActivityDelegate(activity: TurbolinksActivity) : TurbolinksActivity by activity {

    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String) {
        val controller = currentController()
        val presentation = currentPresentation()
        val router = onProvideRouter()
        val bundle = buildBundle(location)
        val previousLocation = previousLocation()

        detachWebViewFromCurrentDestination(destinationIsFinishing = false) {
            if (action == "replace" || presentation == MODAL) {
                // Don't keep the current screen in the backstack
                controller.popBackStack()

                // If the previous location is the same as the new
                // one, don't navigate to new location, so the same
                // locations aren't both in the backstack.
                if (locationsAreSame(location, previousLocation)) {
                    return@detachWebViewFromCurrentDestination
                }
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
        return currentDestination().findNavController()
    }

    private fun currentDestination(): Fragment {
        return onProvideCurrentDestination()
    }

    private fun previousLocation(): String? {
        return currentDestination().arguments?.getString("previousLocation")
    }

    private fun currentPresentation(): TurbolinksPresentation {
        val turbolinksFragment = currentDestination() as? TurbolinksFragment
        return turbolinksFragment?.onProvidePresentation() ?: NORMAL
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

    private fun locationsAreSame(first: String?, second: String?): Boolean {
        fun String.removeInconsequentialSuffix(): String {
            return this.removeSuffix("#").removeSuffix("/")
        }

        return first?.removeInconsequentialSuffix() == second?.removeInconsequentialSuffix()
    }

    private fun buildBundle(location: String): Bundle {
        val currentLocation = currentDestination().arguments?.getString("location")

        return Bundle().apply {
            putString("location", location)
            currentLocation?.let { putString("previousLocation", it) }
        }
    }
}
