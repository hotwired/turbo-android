package com.hotwire.turbo.demo.base

import android.app.Activity
import androidx.fragment.app.Fragment
import com.hotwire.turbo.BuildConfig
import com.hotwire.turbo.config.TurbolinksPathConfiguration
import com.hotwire.turbo.demo.features.imageviewer.ImageViewerFragment
import com.hotwire.turbo.demo.features.profile.ProfileFragment
import com.hotwire.turbo.demo.features.web.WebFragment
import com.hotwire.turbo.demo.features.web.WebHomeFragment
import com.hotwire.turbo.demo.features.web.WebModalFragment
import com.hotwire.turbo.session.TurbolinksSessionNavHostFragment
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
