package com.basecamp.turbolinks.demosimple

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.basecamp.turbolinks.TurbolinksDestination

interface Destination : TurbolinksDestination {
    enum class RouteCommand {
        NAVIGATE,
        OPEN_EXTERNAL,
        STOP
    }

    override fun shouldNavigateTo(newLocation: String): Boolean {
        return when (getRouteCommand(newLocation)) {
            RouteCommand.NAVIGATE -> {
                true
            }
            RouteCommand.OPEN_EXTERNAL -> {
                launchCustomTab(newLocation)
                false
            }
            RouteCommand.STOP -> {
                false
            }
        }
    }

    private fun launchCustomTab(location: String) {
        val context = fragment.context ?: return

        val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .enableUrlBarHiding()
                .addDefaultShareMenuItem()
                .setToolbarColor(context.getColor(R.color.white))
                .build()

        intent.launchUrl(context, Uri.parse(location))
    }

    private fun getRouteCommand(location: String): RouteCommand {
        return when (location.startsWith(Constants.BASE_URL)) {
            true -> RouteCommand.NAVIGATE
            else -> RouteCommand.OPEN_EXTERNAL
        }
    }
}