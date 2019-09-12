package com.basecamp.turbolinks

import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.NavController

class TurbolinksActivityDelegate(
        val activity: AppCompatActivity,
        val turbolinksActivity: TurbolinksActivity,
        val router: TurbolinksRouter) {

    private val sessions = mutableListOf<TurbolinksSession>()

    /*
     * Initialize the Activity with a BackPressedDispatcher that
     * properly handles Fragment navigation with the back button.
     */
    init {
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    fun createSession(sessionName: String): TurbolinksSession {
        sessions.firstOrNull { it.sessionName == sessionName }?.let {
            throw IllegalArgumentException("Session with name '$sessionName' already exists")
        }

        return TurbolinksSession.getNew(sessionName, activity).also {
            sessions.add(it)
        }
    }

    fun getSession(sessionName: String): TurbolinksSession {
        return sessions.first { it.sessionName == sessionName }
    }

    /*
     * Dynamically set the controller graph and start destination,
     * so we can use a simplified navigation graph.
     */
    fun startControllerGraph(controller: NavController, startLocation: String,
                             sessionName: String, navGraph: Int, startDestination: Int) {

        val location = NavArgument.Builder()
            .setDefaultValue(startLocation)
            .build()

        val session = NavArgument.Builder()
            .setDefaultValue(sessionName)
            .build()

        controller.graph = controller.navInflater.inflate(navGraph).apply {
            this.addArgument("location", location)
            this.addArgument("sessionName", session)
            this.startDestination = startDestination
        }
    }

    fun navigate(location: String, action: String = "advance"): Boolean {
        return currentDestination().navigate(location, action)
    }

    fun navigateUp(): Boolean {
        return currentDestination().navigateUp()
    }

    fun navigateBack() {
        currentDestination().navigateBack()
    }

    fun clearBackStack() {
        currentDestination().clearBackStack()
    }

    fun currentDestination(): TurbolinksFragment {
        val currentHostFragment = turbolinksActivity.onProvideCurrentNavHostFragment()
        return currentHostFragment.childFragmentManager.primaryNavigationFragment as TurbolinksFragment
    }
}
