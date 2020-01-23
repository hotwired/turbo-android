package com.basecamp.turbolinks

import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class TurbolinksActivityDelegate(val activity: AppCompatActivity,
                                 val router: TurbolinksRouter,
                                 var currentNavHostFragmentId: Int) {

    private val sessions = mutableListOf<TurbolinksSession>()
    val currentDestination: TurbolinksFragment get() = currentFragment

    /*
     * Initialize the Activity with a BackPressedDispatcher that
     * properly handles Fragment navigation with the back button.
     */
    init {
        activity.onBackPressedDispatcher.addCallback(activity) {
            navigateBack()
        }
    }

    fun createSession(sessionName: String, webView: TurbolinksWebView = TurbolinksWebView(activity)): TurbolinksSession {
        sessions.firstOrNull { it.sessionName == sessionName }?.let {
            throw IllegalArgumentException("Session with name '$sessionName' already exists")
        }

        return TurbolinksSession.getNew(sessionName, activity, webView).also {
            sessions.add(it)
        }
    }

    fun getSession(sessionName: String): TurbolinksSession {
        return sessions.first { it.sessionName == sessionName }
    }

    fun clearSessions() {
        sessions.clear()
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

    fun navigate(location: String, options: VisitOptions = VisitOptions()): Boolean {
        return currentDestination.navigate(location, options)
    }

    fun navigateUp(): Boolean {
        return currentDestination.navigateUp()
    }

    fun navigateBack() {
        currentDestination.navigateBack()
    }

    fun clearBackStack() {
        currentDestination.clearBackStack()
    }

    private val currentFragment: TurbolinksFragment
        get() = navHostFragment.childFragmentManager.primaryNavigationFragment as TurbolinksFragment

    private val navHostFragment: NavHostFragment
        get() = activity.supportFragmentManager.findFragmentById(currentNavHostFragmentId) as? NavHostFragment
            ?: throw IllegalStateException("No current NavHostFragment found")
}
