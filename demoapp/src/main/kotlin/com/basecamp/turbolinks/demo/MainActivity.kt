package com.basecamp.turbolinks.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.basecamp.turbolinks.*
import com.basecamp.turbolinks.PathConfiguration.Location
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TurbolinksActivity {
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
    private val router by lazy { Router(this) }
    private val delegate by lazy { TurbolinksActivityDelegate(this) }
    private val selectedTab get() = tabs[selectedPosition]
    private var selectedPosition = 0

    // ----------------------------------------------------------------------------
    // AppCompatActivity
    // ----------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
        initSessions()
        initBottomTabsListener()
        verifyServerIpAddress(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp()
    }

    override fun onBackPressed() {
        navigateBack()
    }

    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun onProvideSession(fragment: Fragment): TurbolinksSession {
        val controller = fragment.findNavController()
        return tabs.first { it.controller == controller }.session
    }

    override fun onProvideSessionRootLocation(): String? {
        return selectedTab.startLocation
    }

    override fun onProvideRouter(): TurbolinksRouter {
        return router
    }

    override fun onProvideCurrentNavHostFragment(): NavHostFragment {
        return supportFragmentManager.findFragmentById(selectedTab.menuId) as? NavHostFragment ?:
            throw IllegalStateException("No current NavHostFragment found")
    }

    override fun onRequestFinish() {
        finish()
    }

    override fun navigate(location: String, action: String, properties: PathProperties?): Boolean {
        return delegate.navigate(location, action, properties)
    }

    override fun navigateUp(): Boolean {
        return delegate.navigateUp()
    }

    override fun navigateBack() {
        delegate.navigateBack()
    }

    override fun clearBackStack() {
        delegate.clearBackStack()
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun initSessions() {
        val location = Location(assetFilePath = "json/configuration.json")
        tabs.forEach {
            it.session.pathConfiguration.load(location)
            it.session.applyWebViewDefaults()
        }
    }

    private fun initBottomTabsListener() {
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            val tab = tabs.first { it.menuId == item.itemId }
            switchTab(tab)
            true
        }
    }

    private fun switchTab(tab: NavigationTab) {
        if (tab == selectedTab) {
            clearBackStack()
            return
        }

        selectedPosition = tabs.indexOf(tab)
        toggleTabVisibility()
    }

    private fun toggleTabVisibility() {
        tabs.forEach { it.section.isInvisible = it != selectedTab }
    }
}
