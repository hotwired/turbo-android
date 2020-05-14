package com.basecamp.turbolinks

import android.content.Context
import android.os.Bundle
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment

abstract class TurbolinksNavHost : NavHostFragment() {
    abstract val sessionName: String
    abstract val startLocation: String
    abstract val startDestinationId: Int
    abstract val navGraphId: Int
    abstract val pathConfigurationLocation: PathConfiguration.Location

    lateinit var session: TurbolinksSession
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recreateSession()
        initControllerGraph()
    }

    internal fun recreateSession() {
        session = TurbolinksSession.getNew(sessionName, requireActivity(), onCreateWebView(requireActivity()))
        onSessionCreated()
    }

    open fun onSessionCreated() {
        session.pathConfiguration.load(pathConfigurationLocation)
    }

    open fun onCreateWebView(context: Context): TurbolinksWebView {
        return TurbolinksWebView(context, null)
    }

    fun reset() {
        session.reset()
        session.rootLocation = startLocation
        initControllerGraph()
    }

    val currentDestination: TurbolinksDestination
        get() = childFragmentManager.primaryNavigationFragment as TurbolinksDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    // Private

    private fun initControllerGraph() {
        val location = NavArgument.Builder()
            .setDefaultValue(startLocation)
            .build()

        val session = NavArgument.Builder()
            .setDefaultValue(sessionName)
            .build()

        navController.graph = navController.navInflater.inflate(navGraphId).apply {
            this.addArgument("location", location)
            this.addArgument("sessionName", session)
            this.startDestination = startDestinationId
        }
    }
}
