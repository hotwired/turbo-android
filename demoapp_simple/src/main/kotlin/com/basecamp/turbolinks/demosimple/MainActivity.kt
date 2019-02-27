package com.basecamp.turbolinks.demosimple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import com.basecamp.turbolinks.TurbolinksActivity
import com.basecamp.turbolinks.TurbolinksActivityDelegate
import com.basecamp.turbolinks.TurbolinksRouter
import com.basecamp.turbolinks.TurbolinksSession

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    private val fragmentId = R.id.section_food_nav
    private val session by lazy { TurbolinksSession.getNew(this) }
    private val controller by lazy { findNavController(fragmentId) }
    private val view by lazy { layoutInflater.inflate(R.layout.activity_main, null) }
    private val router by lazy { Router(this) }
    private val delegate by lazy { TurbolinksActivityDelegate(this) }

    // ----------------------------------------------------------------------------
    // AppCompatActivity
    // ----------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
        initControllerGraph()
        initWebView()
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
        return session
    }

    override fun onProvideRouter(): TurbolinksRouter {
        return router
    }

    override fun onProvideCurrentDestination(): Fragment {
        val host = supportFragmentManager.findFragmentById(fragmentId)
        return host?.childFragmentManager?.primaryNavigationFragment ?:
                throw IllegalStateException("No current destination found")
    }

    override fun onRequestFinish() {
        finish()
    }

    override fun navigate(location: String, action: String) {
        delegate.navigate(location, action)
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

    private fun initControllerGraph() {
        // Dynamically set the controller graph and start destination,
        // so we can use a simplified navigation graph.
        val startLocation = NavArgument.Builder().setDefaultValue(Constants.FOOD_URL).build()

        controller.graph = controller.navInflater.inflate(R.navigation.nav_graph).apply {
            addArgument("location", startLocation)
            startDestination = R.id.food_fragment
        }
    }

    private fun initWebView() {
        session.applyWebViewDefaults()
    }
}
