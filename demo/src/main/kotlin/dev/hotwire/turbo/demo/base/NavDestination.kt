package dev.hotwire.turbo.demo.base

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.util.BASE_URL
import dev.hotwire.turbo.nav.TurboNavDestination

interface NavDestination : TurboNavDestination {
    override fun shouldNavigateTo(newLocation: String): Boolean {
        return if (isNavigable(newLocation)) {
            true
        } else {
            launchCustomTab(newLocation)
            false
        }
    }

    private fun isNavigable(location: String): Boolean {
        return location.startsWith(BASE_URL)
    }

    private fun launchCustomTab(location: String) {
        val context = fragment.context ?: return

        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .enableUrlBarHiding()
            .addDefaultShareMenuItem()
            .setToolbarColor(context.getColor(R.color.color_surface))
            .build()
            .launchUrl(context, Uri.parse(location))
    }
}
