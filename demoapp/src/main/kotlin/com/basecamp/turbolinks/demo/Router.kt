package com.basecamp.turbolinks.demo

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.basecamp.turbolinks.PathProperties
import com.basecamp.turbolinks.PresentationContext
import com.basecamp.turbolinks.TurbolinksRouter
import com.basecamp.turbolinks.context

class Router(private val context: Context) : TurbolinksRouter() {
    override fun getNavigationAction(location: String, properties: PathProperties): Int? {
        return when (getRouteCommand(location)) {
            RouteCommand.OPEN_EXTERNAL -> {
                launchChromeCustomTab(context, location)
                null
            }
            RouteCommand.NAVIGATE -> {
                getRouteAction(properties)
            }
        }
    }

    override fun getModalContextStartAction(location: String): Int {
        return R.id.action_turbolinks_modal_start
    }

    override fun getModalContextDismissAction(location: String): Int {
        return R.id.action_turbolinks_modal_dismiss
    }

    fun getRouteCommand(location: String): RouteCommand {
        return when (location.startsWith(Constants.BASE_URL)) {
            true -> RouteCommand.NAVIGATE
            else -> RouteCommand.OPEN_EXTERNAL
        }
    }

    fun getRouteAction(properties: PathProperties): Int {
        if (properties.context == PresentationContext.MODAL) {
            // Navigating within an existing modal context
            return R.id.action_turbolinks_modal
        }

        return when (properties.type) {
            RouteDestination.WEB -> R.id.action_turbolinks
            RouteDestination.IMAGE -> R.id.action_image_viewer
        }
    }

    private fun launchChromeCustomTab(context: Context, location: String) {
        val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .enableUrlBarHiding()
                .addDefaultShareMenuItem()
                .setToolbarColor(context.color(R.color.white))
                .build()

        intent.launchUrl(context, Uri.parse(location))
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
