package dev.hotwire.turbo.demo.main

import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.hotwire.turbo.BuildConfig
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.demo.features.imageviewer.ImageViewerFragment
import dev.hotwire.turbo.demo.features.numbers.NumberBottomSheetFragment
import dev.hotwire.turbo.demo.features.numbers.NumbersFragment
import dev.hotwire.turbo.demo.features.web.WebBottomSheetFragment
import dev.hotwire.turbo.demo.features.web.WebFragment
import dev.hotwire.turbo.demo.features.web.WebHomeFragment
import dev.hotwire.turbo.demo.features.web.WebModalFragment
import dev.hotwire.turbo.demo.util.HOME_URL
import dev.hotwire.turbo.demo.util.initDayNightTheme
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import kotlin.reflect.KClass

@Suppress("unused")
class MainSessionNavHostFragment : TurboSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation = HOME_URL

    override val registeredActivities: List<KClass<out AppCompatActivity>>
        get() = listOf()

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            WebFragment::class,
            WebHomeFragment::class,
            WebModalFragment::class,
            WebBottomSheetFragment::class,
            NumbersFragment::class,
            NumberBottomSheetFragment::class,
            ImageViewerFragment::class
        )

    override val pathConfigurationLocation: TurboPathConfiguration.Location
        get() = TurboPathConfiguration.Location(
            assetFilePath = "json/configuration.json"
        )

    override fun onSessionCreated() {
        super.onSessionCreated()
        session.webView.settings.userAgentString = customUserAgent(session.webView)
        session.webView.initDayNightTheme()

        if (BuildConfig.DEBUG) {
            session.setDebugLoggingEnabled(true)
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    private fun customUserAgent(webView: WebView): String {
        return "Turbo Native Android ${webView.settings.userAgentString}"
    }
}
