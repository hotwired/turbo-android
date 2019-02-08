package com.basecamp.turbolinks.demosimple

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
            initWebView()
            setupToolbar()
            initDebugLogging()
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
                    Router.getRouteAction(location, isAtStartDestination())
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

    private fun initWebView() {
        // Clear the WebView cache so assets aren't cached between
        // app launches during development on the server.
        WebView(this).clearCache(true)
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

    private fun initDebugLogging() {
        if (BuildConfig.DEBUG) {
            session.enableDebugLogging = true
            WebView.setWebContentsDebuggingEnabled(true)
        }
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
