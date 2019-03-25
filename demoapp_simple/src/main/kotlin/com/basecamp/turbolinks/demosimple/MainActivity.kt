package com.basecamp.turbolinks.demosimple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.basecamp.turbolinks.*
import com.basecamp.turbolinks.PathConfiguration.Location

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    private val hostFragmentId = R.id.section_food_nav
    private val session by lazy { TurbolinksSession.getNew(this) }
    private val controller by lazy { findNavController(hostFragmentId) }
    private val view by lazy { layoutInflater.inflate(R.layout.activity_main, null) }
    private val router by lazy { Router(this) }
    private val delegate by lazy { TurbolinksActivityDelegate(this) }
    private val startLocation = Constants.FOOD_URL

    // ----------------------------------------------------------------------------
    // AppCompatActivity
    // ----------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
        initControllerGraph()
        initSession()
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

    override fun onProvideSessionRootLocation(): String? {
        return startLocation
    }

    override fun onProvideRouter(): TurbolinksRouter {
        return router
    }

    override fun onProvideCurrentNavHostFragment(): NavHostFragment {
        return supportFragmentManager.findFragmentById(hostFragmentId) as? NavHostFragment ?:
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

    private fun initControllerGraph() {
        // Dynamically set the controller graph and start destination,
        // so we can use a simplified navigation graph.
        val startLocation = NavArgument.Builder().setDefaultValue(startLocation).build()

        controller.graph = controller.navInflater.inflate(R.navigation.nav_graph).apply {
            addArgument("location", startLocation)
            startDestination = R.id.food_fragment
        }
    }

    private fun initSession() {
        val location = Location(assetFilePath = "json/configuration.json")
        session.pathConfiguration.load(location)
        session.applyWebViewDefaults()
    }
}
