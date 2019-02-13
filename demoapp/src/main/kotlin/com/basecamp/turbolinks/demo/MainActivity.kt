package com.basecamp.turbolinks.demo

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
    private val foodTab by lazy { NavigationTab(
            session = TurbolinksSession.getNew(this),
            controller = findNavController(R.id.section_food_nav),
            startLocation = Constants.FOOD_URL,
            startDestination = R.id.food_fragment,
            menuId = R.id.section_food_nav,
            section = section_food
    )}

    private val ordersTab by lazy { NavigationTab(
            session = TurbolinksSession.getNew(this),
            controller = findNavController(R.id.section_orders_nav),
            startLocation = Constants.ORDERS_URL,
            startDestination = R.id.orders_fragment,
            menuId = R.id.section_orders_nav,
            section = section_orders
    )}

    private val meTab by lazy { NavigationTab(
            session = TurbolinksSession.getNew(this),
            controller = findNavController(R.id.section_me_nav),
            startLocation = null,
            startDestination = R.id.me_fragment,
            menuId = R.id.section_me_nav,
            section = section_me
    )}

    private val tabs by lazy { arrayOf(foodTab, ordersTab, meTab) }
    private val selectedTab get() = tabs[selectedPosition]
    private var selectedPosition = 0

    override val listener = object : Listener {
        override fun onActivityCreated() {
            initWebViews()
            setupToolbar()
            initBottomTabsListener()
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
            val tab = tabs.first { it.menuId == item.itemId }
            if (tab == selectedTab) {
                clearBackStack()
                return@setOnNavigationItemSelectedListener true
            }

            selectedPosition = tabs.indexOf(tab)
            toggleTabVisibility()
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
        return selectedTab.controller
    }

    private fun activeDestination(): Fragment? {
        val fragmentId = selectedTab.menuId
        val host = supportFragmentManager.findFragmentById(fragmentId)
        return host?.childFragmentManager?.fragments?.lastOrNull()
    }

    private fun activeSession(fragment: TurbolinksFragment): TurbolinksSession {
        val controller = fragment.findNavController()
        return tabs.first { it.controller == controller }.session
    }

    private fun initWebViews() {
        tabs.forEach { it.session.applyWebViewDefaults() }
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

    private fun toggleTabVisibility() {
        tabs.forEach {
            it.section.isInvisible = it != selectedTab
        }
    }

    private fun toggleFullScreen(enabled: Boolean) {
        TransitionManager.beginDelayedTransition(view, ChangeBounds().apply { duration = 150 })
        app_bar.isVisible = !enabled
        bottom_nav.isVisible = !enabled
    }
}
