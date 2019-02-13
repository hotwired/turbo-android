package com.basecamp.turbolinks.demosimple

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.basecamp.turbolinks.TurbolinksActivity
import com.basecamp.turbolinks.TurbolinksFragment
import com.basecamp.turbolinks.TurbolinksSession
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.error.view.*

class MainActivity : TurbolinksActivity() {
    private val fragmentId = R.id.section_food_nav
    private val session by lazy { TurbolinksSession.getNew(this) }

    override val listener = object : Listener {
        override fun onActivityCreated() {
            initControllerGraph()
            initWebView()
            setupToolbar()
            verifyServerIpAddress(this@MainActivity)
        }

        override fun onProvideView(): ViewGroup {
            return layoutInflater.inflate(R.layout.activity_main, null) as ViewGroup
        }

        override fun onProvideProgressView(location: String): View {
            return layoutInflater.inflate(R.layout.progress, null)
        }

        override fun onProvideErrorView(statusCode: Int): View {
            return layoutInflater.inflate(R.layout.error, null).apply {
                error_message.text = Error.getMessage(statusCode)
            }
        }

        override fun onProvideNavController(): NavController {
            return activeNavController()
        }

        override fun onProvideCurrentDestination(): Fragment? {
            return activeDestination()
        }

        override fun onProvideNavigationAction(location: String): Int? {
            return when (Router.getRouteCommand(location)) {
                RouteCommand.OPEN_EXTERNAL -> {
                    Router.launchChromeCustomTab(this@MainActivity, location)
                    null
                }
                RouteCommand.NAVIGATE -> {
                    Router.getRouteAction(location)
                }
            }
        }

        override fun onProvideSession(fragment: TurbolinksFragment): TurbolinksSession {
            return session
        }

        override fun onDestinationTitleChanged(title: String) {
            updateToolbarTitle(title)
        }

        override fun onDestinationReplaced() {
            setupToolbar()
        }

        override fun onNavigatedForward() {
            setupToolbar()
        }

        override fun onNavigatedBackward() {
            setupToolbar()
        }

        override fun onBackStackCleared() {
            setupToolbar()
        }

        override fun onRequestFullscreen() {
            toggleFullScreen(true)
        }

        override fun onRequestExitFullscreen() {
            toggleFullScreen(false)
        }
    }

    private fun activeNavController(): NavController {
        return findNavController(R.id.section_food_nav)
    }

    private fun activeDestination(): Fragment? {
        val host = supportFragmentManager.findFragmentById(fragmentId)
        return host?.childFragmentManager?.fragments?.lastOrNull()
    }

    private fun initControllerGraph() {
        val controller = activeNavController()
        val startLocation = NavArgument.Builder().setDefaultValue(Constants.FOOD_URL).build()

        controller.graph = controller.navInflater.inflate(R.navigation.nav_graph).apply {
            addArgument("location", startLocation)
            startDestination = R.id.food_fragment
        }
    }

    private fun initWebView() {
        session.applyWebViewDefaults()
    }

    private fun setupToolbar() {
        val controller = activeNavController()
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(controller, AppBarConfiguration(controller.graph))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        app_bar_logo.isInvisible = !isAtStartDestination()
    }

    private fun updateToolbarTitle(title: String) {
        toolbar.title = if (isAtStartDestination()) "" else title
    }

    private fun toggleFullScreen(enabled: Boolean) {
        TransitionManager.beginDelayedTransition(view, ChangeBounds().apply { duration = 150 })
        app_bar.isVisible = !enabled
    }
}
