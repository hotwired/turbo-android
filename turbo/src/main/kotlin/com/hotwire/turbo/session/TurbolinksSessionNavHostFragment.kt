package com.hotwire.turbo.session

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.hotwire.turbo.config.TurbolinksPathConfiguration
import com.hotwire.turbo.nav.TurbolinksNavDestination
import com.hotwire.turbo.nav.TurbolinksNavGraphBuilder
import com.hotwire.turbo.views.TurbolinksWebView
import kotlin.reflect.KClass

abstract class TurbolinksSessionNavHostFragment : NavHostFragment() {
    abstract val sessionName: String
    abstract val startLocation: String
    abstract val pathConfigurationLocation: TurbolinksPathConfiguration.Location
    abstract val registeredActivities: List<KClass<out Activity>>
    abstract val registeredFragments: List<KClass<out Fragment>>

    lateinit var session: TurbolinksSession
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNewSession()
        initControllerGraph()
    }

    internal fun createNewSession() {
        session = TurbolinksSession.getNew(sessionName, requireActivity(), onCreateWebView(requireActivity()))
        onSessionCreated()
    }

    open fun onSessionCreated() {
        session.pathConfiguration.load(pathConfigurationLocation)
    }

    open fun onCreateWebView(context: Context): TurbolinksWebView {
        return TurbolinksWebView(context, null)
    }

    fun reset(onReset: () -> Unit = {}) {
        currentNavDestination.clearBackStack {
            session.reset()
            initControllerGraph()
            onReset()
        }
    }

    val currentNavDestination: TurbolinksNavDestination
        get() = childFragmentManager.primaryNavigationFragment as TurbolinksNavDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    private fun initControllerGraph() {
        navController.apply {
            graph = TurbolinksNavGraphBuilder(
                startLocation = startLocation,
                pathConfiguration = session.pathConfiguration,
                navController = findNavController()
            ).build(
                registeredActivities = registeredActivities,
                registeredFragments = registeredFragments
            )
        }
    }
}
