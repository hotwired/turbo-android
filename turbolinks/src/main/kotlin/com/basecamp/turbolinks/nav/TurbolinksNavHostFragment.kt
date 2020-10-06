package com.basecamp.turbolinks.nav

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.*
import com.basecamp.turbolinks.core.TurbolinksDestination
import com.basecamp.turbolinks.core.Turbolinks
import com.basecamp.turbolinks.views.TurbolinksWebView
import com.basecamp.turbolinks.config.PathConfiguration
import kotlin.reflect.KClass

abstract class TurbolinksNavHostFragment : NavHostFragment() {
    abstract val turbolinksName: String
    abstract val startLocation: String
    abstract val pathConfigurationLocation: PathConfiguration.Location
    abstract val registeredActivities: List<KClass<out Activity>>
    abstract val registeredFragments: List<KClass<out Fragment>>

    lateinit var turbolinks: Turbolinks
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createTurbolinksInstance()
        initControllerGraph()
    }

    internal fun createTurbolinksInstance() {
        turbolinks = Turbolinks.getNew(turbolinksName, requireActivity(), onCreateWebView(requireActivity()))
        onTurbolinksCreated()
    }

    open fun onTurbolinksCreated() {
        turbolinks.pathConfiguration.load(pathConfigurationLocation)
    }

    open fun onCreateWebView(context: Context): TurbolinksWebView {
        return TurbolinksWebView(context, null)
    }

    fun reset() {
        turbolinks.reset()
        turbolinks.rootLocation = startLocation
        initControllerGraph()
    }

    val currentDestination: TurbolinksDestination
        get() = childFragmentManager.primaryNavigationFragment as TurbolinksDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    private fun initControllerGraph() {
        navController.apply {
            graph = TurbolinksNavGraphBuilder(
                startLocation = startLocation,
                pathConfiguration = turbolinks.pathConfiguration,
                navController = findNavController()
            ).build(
                registeredActivities = registeredActivities,
                registeredFragments = registeredFragments
            )
        }
    }
}
