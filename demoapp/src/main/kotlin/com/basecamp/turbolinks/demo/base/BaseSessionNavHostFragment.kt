package com.basecamp.turbolinks.demo.base

import android.app.Activity
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.BuildConfig
import com.basecamp.turbolinks.config.TurbolinksPathConfiguration
import com.basecamp.turbolinks.demo.features.imageviewer.ImageViewerFragment
import com.basecamp.turbolinks.demo.features.profile.ProfileFragment
import com.basecamp.turbolinks.demo.features.web.WebFragment
import com.basecamp.turbolinks.demo.features.web.WebHomeFragment
import com.basecamp.turbolinks.demo.features.web.WebModalFragment
import com.basecamp.turbolinks.session.TurbolinksSessionNavHostFragment
import kotlin.reflect.KClass

abstract class BaseSessionNavHostFragment : TurbolinksSessionNavHostFragment() {
    override val registeredActivities: List<KClass<out Activity>>
        get() = listOf()

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            WebFragment::class,
            WebHomeFragment::class,
            WebModalFragment::class,
            ProfileFragment::class,
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
