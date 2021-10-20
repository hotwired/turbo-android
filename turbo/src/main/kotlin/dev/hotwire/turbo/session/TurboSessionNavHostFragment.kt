package dev.hotwire.turbo.session

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.nav.TurboNavGraphBuilder
import dev.hotwire.turbo.views.TurboWebView
import kotlin.reflect.KClass

abstract class TurboSessionNavHostFragment : NavHostFragment() {
    abstract val sessionName: String
    abstract val startLocation: String
    abstract val pathConfigurationLocation: TurboPathConfiguration.Location
    abstract val registeredActivities: List<KClass<out AppCompatActivity>>
    abstract val registeredFragments: List<KClass<out Fragment>>

    lateinit var session: TurboSession
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNewSession()
        initControllerGraph()
    }

    internal fun createNewSession() {
        val activity = requireActivity() as AppCompatActivity
        session = TurboSession(sessionName, activity, onCreateWebView(activity))
        onSessionCreated()
    }

    open fun onSessionCreated() {
        session.pathConfiguration.load(pathConfigurationLocation)
    }

    open fun onCreateWebView(context: Context): TurboWebView {
        return TurboWebView(context, null)
    }

    fun reset(onReset: () -> Unit = {}) {
        currentNavDestination.delegate().navigator.onNavigationVisit {
            currentNavDestination.clearBackStack {
                session.reset()
                initControllerGraph()
                onReset()
            }
        }
    }

    val currentNavDestination: TurboNavDestination
        get() = childFragmentManager.primaryNavigationFragment as TurboNavDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    private fun initControllerGraph() {
        navController.apply {
            graph = TurboNavGraphBuilder(
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
