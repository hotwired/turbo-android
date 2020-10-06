package com.basecamp.turbolinks.demosimple

import android.app.Activity
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.BuildConfig
import com.basecamp.turbolinks.config.PathConfiguration
import com.basecamp.turbolinks.nav.TurbolinksNavHostFragment
import kotlin.reflect.KClass

@Suppress("unused")
class MainNavHostFragment : TurbolinksNavHostFragment() {
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

    override val pathConfigurationLocation: PathConfiguration.Location
        get() = PathConfiguration.Location(
            assetFilePath = "json/configuration.json"
        )

    override fun onSessionCreated() {
        super.onSessionCreated()
        session.setDebugLoggingEnabled(BuildConfig.DEBUG)
    }
}