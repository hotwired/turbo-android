package com.basecamp.turbolinks.demo

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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

    private val view by lazy { layoutInflater.inflate(R.layout.activity_main, null) }
    private val tabs by lazy { arrayOf(foodTab, ordersTab, meTab) }
    private val selectedTab get() = tabs[selectedPosition]
    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
        initWebViews()
        initBottomTabsListener()
        verifyServerIpAddress(this)
    }

    override val listener = object : Listener {
        override fun onProvideProgressView(location: String): View {
            return layoutInflater.inflate(R.layout.progress, null)
        }

        override fun onProvideErrorView(statusCode: Int): View {
            return layoutInflater.inflate(R.layout.error, null).apply {
                error_message.text = Error.getMessage(statusCode)
            }
        }

        override fun onProvideNavController(): NavController {
            return selectedTab.controller
        }

        override fun onProvideCurrentDestination(): Fragment? {
            val fragmentId = selectedTab.menuId
            val host = supportFragmentManager.findFragmentById(fragmentId)
            return host?.childFragmentManager?.fragments?.lastOrNull()
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
            val controller = fragment.findNavController()
            return tabs.first { it.controller == controller }.session
        }

        override fun onRequestEnterModalPresentation() {
            toggleModalPresentation(true)
        }

        override fun onRequestExitModalPresentation() {
            toggleModalPresentation(false)
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
            true
        }
    }

    private fun initWebViews() {
        tabs.forEach { it.session.applyWebViewDefaults() }
    }

    private fun toggleTabVisibility() {
        tabs.forEach { it.section.isInvisible = it != selectedTab }
    }

    private fun toggleModalPresentation(modal: Boolean) {
        val startY = if (modal) 0 else bottom_nav.height
        val endY = if (modal) bottom_nav.height else 0

        bottom_nav.translationYAnimator(
                startY = startY.toFloat(),
                endY = endY.toFloat(),
                duration = 200
        ).start()
    }
}
