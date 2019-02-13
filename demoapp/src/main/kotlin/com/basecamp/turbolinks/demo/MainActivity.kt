package com.basecamp.turbolinks.demo

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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
    private val navMenuIds = arrayOf(R.id.section_food_nav, R.id.section_orders_nav, R.id.section_me_nav)
    private val controllers by lazy { navMenuIds.map { findNavController(it) } }
    private val sessions by lazy { Array(3) { TurbolinksSession.getNew(this) } }
    private val sections by lazy { arrayOf(section_food, section_orders, section_me) }

    private var selectionPosition = 0

    override val listener = object : Listener {
        override fun onActivityCreated() {
            initControllerGraphs()
            initWebViews()
            setupToolbar()
            initBottomTabsListener()
            verifyServerIpAddress()
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
            return activeSession(fragment)
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

    private fun initBottomTabsListener() {
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            val newPosition = navMenuIds.indexOfFirst { it == item.itemId }
            if (newPosition == selectionPosition) {
                clearBackStack()
                return@setOnNavigationItemSelectedListener true
            }

            selectionPosition = newPosition
            sections.forEachIndexed { index, view ->
                view.isInvisible = index != selectionPosition
            }

            setupToolbar()
            updateToolbarTitle(activeDestinationTitle())
            true
        }
    }

    private fun activeDestinationTitle(): String {
        val fragment = activeDestination()
        return (fragment as NavigationFragment?)?.provideTitle() ?: ""
    }

    private fun activeNavController(): NavController {
        return controllers[selectionPosition]
    }

    private fun activeDestination(): Fragment? {
        val fragmentId = navMenuIds[selectionPosition]
        val host = supportFragmentManager.findFragmentById(fragmentId)
        return host?.childFragmentManager?.fragments?.lastOrNull()
    }

    private fun activeSession(fragment: TurbolinksFragment): TurbolinksSession {
        val controller = fragment.findNavController()
        val position = controllers.indexOfFirst { it == controller }
        return sessions[position]
    }

    private fun initControllerGraphs() {
        // Dynamically set the controller graphs and start destinations,
        // so we can use a shared, common navigation graph between tabs.
        val startDestinations = listOf(R.id.food_fragment, R.id.orders_fragment, R.id.me_fragment)

        controllers.forEachIndexed { index, controller ->
            controller.graph = controller.navInflater.inflate(R.navigation.nav_graph).apply {
                startDestination = startDestinations[index]
            }
        }
    }

    private fun initWebViews() {
        sessions.forEach { it.applyWebViewDefaults() }
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
        bottom_nav.isVisible = !enabled
    }

    @Suppress("ConstantConditionIf")
    private fun verifyServerIpAddress() {
        if (Constants.IP_ADDRESS == "x.x.x.x") {
            AlertDialog.Builder(this).apply {
                setTitle(context.getString(R.string.server_ip_warning))
                setMessage(context.getString(R.string.server_ip_warning_message))
                setPositiveButton(R.string.server_ip_warning_button) { _, _ -> }
            }.create().show()
        }
    }
}
