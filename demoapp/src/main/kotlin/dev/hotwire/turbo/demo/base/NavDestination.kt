package dev.hotwire.turbo.demo.base

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.main.MainActivity
import dev.hotwire.turbo.demo.util.Constants
import dev.hotwire.turbo.nav.TurboNavDestination
import java.net.MalformedURLException

interface NavDestination : TurboNavDestination {
    enum class RouteCommand {
        STOP,
        NAVIGATE,
        OPEN_EXTERNAL
    }

    override fun shouldNavigateTo(newLocation: String): Boolean {
        return when (getRouteCommand(newLocation)) {
            RouteCommand.STOP -> {
                false
            }
            RouteCommand.NAVIGATE -> {
                true
            }
            RouteCommand.OPEN_EXTERNAL -> {
                launchCustomTab(newLocation)
                false
            }
        }
    }

    fun displaysBottomTabs(): Boolean {
        return true
    }

    fun animateBottomNavVisibility() {
        (fragment.activity as? MainActivity)?.apply {
            animateBottomNavVisibility(fragment, displaysBottomTabs())
        }
    }

    private fun getRouteCommand(location: String): RouteCommand {
        return when {
            isInvalidUrl(location) -> RouteCommand.STOP
            isNavigable(location) -> RouteCommand.NAVIGATE
            else -> RouteCommand.OPEN_EXTERNAL
        }
    }

    private fun isNavigable(location: String): Boolean {
        return location.startsWith(Constants.BASE_URL)
    }

    private fun isInvalidUrl(location: String): Boolean {
        return try {
            session.pathConfiguration.properties(location)
            false
        } catch (e: MalformedURLException) {
            true
        }
    }

    private fun launchCustomTab(location: String) {
        val context = fragment.context ?: return

        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .enableUrlBarHiding()
            .addDefaultShareMenuItem()
            .setToolbarColor(context.getColor(R.color.white))
            .build()
            .launchUrl(context, Uri.parse(location))
    }
}
