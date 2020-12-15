package dev.hotwire.turbo.demo.base

import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
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
        val color = context.getColor(R.color.color_surface)
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(color)
            .setNavigationBarColor(color)
            .build()

        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(SHARE_STATE_ON)
            .setUrlBarHidingEnabled(false)
            .setDefaultColorSchemeParams(colorParams)
            .build()
            .launchUrl(context, Uri.parse(location))
    }
}
