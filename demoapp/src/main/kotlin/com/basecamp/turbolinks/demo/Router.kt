package com.basecamp.turbolinks.demo

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
                location.endsWith("/edit") -> RouteDestination.WEB_FORM
                else -> RouteDestination.WEB
            }
        }

        fun getRouteAction(location: String, isAtStartDestination: Boolean): Int {
            return when (getRouteDestination(location)) {
                RouteDestination.IMAGE -> R.id.action_turbolinks_to_image_viewer
                RouteDestination.WEB_FORM -> R.id.action_home_to_form
                RouteDestination.WEB -> {
                    // Distinct actions on home vs other destinations are necessary
                    // so the Navigation Component knows when to properly display
                    // the Up navigation arrow in the toolbar.
                    if (isAtStartDestination) R.id.action_home_to_turbolinks
                    else R.id.action_turbolinks_to_turbolinks
                }
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
    WEB,
    WEB_FORM
}
