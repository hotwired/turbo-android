package com.basecamp.turbolinks.demosimple.main

import android.app.Activity
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.BuildConfig
import com.basecamp.turbolinks.config.TurbolinksPathConfiguration
import com.basecamp.turbolinks.demosimple.features.imageviewer.ImageViewerFragment
import com.basecamp.turbolinks.demosimple.features.web.WebFragment
import com.basecamp.turbolinks.demosimple.features.web.WebHomeFragment
import com.basecamp.turbolinks.demosimple.util.Constants
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment
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