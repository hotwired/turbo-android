package dev.hotwire.turbo.demosimple.base

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import dev.hotwire.turbo.demosimple.R
import dev.hotwire.turbo.demosimple.util.Constants
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
