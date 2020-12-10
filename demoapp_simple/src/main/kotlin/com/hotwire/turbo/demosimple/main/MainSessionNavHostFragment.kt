package com.hotwire.turbo.demosimple.main

import android.app.Activity
import androidx.fragment.app.Fragment
import com.hotwire.turbo.BuildConfig
import com.hotwire.turbo.config.TurbolinksPathConfiguration
import com.hotwire.turbo.demosimple.features.imageviewer.ImageViewerFragment
import com.hotwire.turbo.demosimple.features.web.WebFragment
import com.hotwire.turbo.demosimple.features.web.WebHomeFragment
import com.hotwire.turbo.demosimple.util.Constants
import com.hotwire.turbo.session.TurbolinksSessionNavHostFragment
import kotlin.reflect.KClass

@Suppress("unused")
class MainSessionNavHostFragment : TurbolinksSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation
        get() = Constants.FOOD_URL

    override val registeredActivities: List<KClass<out Activity>>
        get() = listOf()

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            WebFragment::class,
            WebHomeFragment::class,
            ImageViewerFragment::class
        )

    override val pathConfigurationLocation: TurbolinksPathConfiguration.Location
        get() = TurbolinksPathConfiguration.Location(
            assetFilePath = "json/configuration.json"
        )

    override fun onSessionCreated() {
        super.onSessionCreated()
        session.setDebugLoggingEnabled(BuildConfig.DEBUG)
    }
}