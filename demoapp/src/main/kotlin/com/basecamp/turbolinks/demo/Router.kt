package com.basecamp.turbolinks.demo

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.basecamp.turbolinks.PresentationContext
import com.basecamp.turbolinks.TurbolinksRouter

class Router(private val context: Context) : TurbolinksRouter() {
    override fun getNavigationAction(location: String): Int? {
        return when (getRouteCommand(location)) {
            RouteCommand.OPEN_EXTERNAL -> {
                launchChromeCustomTab(context, location)
                null
            }
            RouteCommand.NAVIGATE -> {
                getRouteAction(location)
            }
        }
    }

    override fun getModalContextStartAction(location: String): Int {
        return R.id.action_turbolinks_modal_start
    }

    override fun getModalContextDismissAction(location: String): Int {
        return R.id.action_turbolinks_modal_dismiss
    }

    override fun getPresentationContext(location: String): PresentationContext {
        return when (isModalContext(location)) {
            true -> PresentationContext.MODAL
            else -> PresentationContext.DEFAULT
        }
    }

    fun getRouteCommand(location: String): RouteCommand {
        return when(location.startsWith(Constants.BASE_URL)) {
            true -> RouteCommand.NAVIGATE
            else -> RouteCommand.OPEN_EXTERNAL
        }
    }

    fun getRouteDestination(location: String): RouteDestination {
        return when {
            location.endsWith(".png") -> RouteDestination.IMAGE
            isModalContext(location) -> RouteDestination.WEB_MODAL
            else -> RouteDestination.WEB
        }
    }

    fun getRouteAction(location: String): Int {
        return when (getRouteDestination(location)) {
            RouteDestination.IMAGE -> R.id.action_image_viewer
            RouteDestination.WEB_MODAL -> R.id.action_turbolinks_modal
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

    private fun isModalContext(location: String): Boolean {
        return location.endsWith("/edit") || location.endsWith("/new")
    }
}

enum class RouteCommand {
    NAVIGATE,
    OPEN_EXTERNAL
}

enum class RouteDestination {
    IMAGE,
    WEB,
    WEB_MODAL
}
