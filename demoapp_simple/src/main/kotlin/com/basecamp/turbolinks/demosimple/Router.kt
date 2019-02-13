package com.basecamp.turbolinks.demosimple

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

class Router {
    companion object {
        fun getRouteCommand(location: String): RouteCommand {
            return when(location.startsWith(Constants.BASE_URL)) {
                true -> RouteCommand.NAVIGATE
                else -> RouteCommand.OPEN_EXTERNAL
            }
        }

        fun getRouteDestination(location: String): RouteDestination {
            return when {
                location.endsWith(".png") -> RouteDestination.IMAGE
                else -> RouteDestination.WEB
            }
        }

        fun getRouteAction(location: String): Int {
            return when (getRouteDestination(location)) {
                RouteDestination.IMAGE -> R.id.action_image_viewer
                RouteDestination.WEB -> R.id.action_turbolinks
            }
        }

        fun launchChromeCustomTab(context: Context, location: String) {
            val intent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .enableUrlBarHiding()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(context.color(R.color.white))
                    .build()

            intent.launchUrl(context, Uri.parse(location))
        }
    }
}

enum class RouteCommand {
    NAVIGATE,
    OPEN_EXTERNAL
}

enum class RouteDestination {
    IMAGE,
    WEB
}
