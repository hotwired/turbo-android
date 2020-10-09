package com.basecamp.turbolinks.config

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.basecamp.turbolinks.config.TurbolinksPathConfiguration.Location
import com.basecamp.turbolinks.nav.TurbolinksNavPresentationContext
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksPathConfigurationTest {
    private lateinit var context: Context
    private lateinit var pathConfiguration: TurbolinksPathConfiguration
    private val mockRepository = mock<TurbolinksPathConfigurationRepository>()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pathConfiguration = TurbolinksPathConfiguration(context).apply {
            load(Location(assetFilePath = "json/test-configuration.json"))
        }
    }

    @Test
    fun assetConfigurationIsLoaded() {
        assertThat(pathConfiguration.rules.size).isEqualTo(6)
    }

    @Test
    fun presentationContext() {
        val url = "http://example.com"

        assertThat(pathConfiguration.properties("$url/home").context).isEqualTo(TurbolinksNavPresentationContext.DEFAULT)
        assertThat(pathConfiguration.properties("$url/new").context).isEqualTo(TurbolinksNavPresentationContext.MODAL)
        assertThat(pathConfiguration.properties("$url/edit").context).isEqualTo(TurbolinksNavPresentationContext.MODAL)
    }

    @Test
    fun remoteConfigurationIsFetched() {
        pathConfiguration.loader.repository = mockRepository

        runBlocking {
            val url = "http://example.com/configuration.json"
            val location = Location(remoteFileUrl = url)

            pathConfiguration.load(location)
            verify(mockRepository).getCachedConfigurationForUrl(context, url)
            verify(mockRepository).getRemoteConfiguration(url)
        }
    }

    @Test
    fun globalProperty() {
        assertThat(pathConfiguration.settings.size).isEqualTo(2)
        assertThat(pathConfiguration.settings["app_latest_version_code"]).isEqualTo("85")
        assertThat(pathConfiguration.settings["custom_app_feature_enabled"]).isEqualTo("true")
        assertThat(pathConfiguration.settings["no_such_key"]).isNull()
    }

    @Test
    fun pullToRefresh() {
        val url = "http://example.com"

        assertThat(pathConfiguration.properties("$url/home").pullToRefreshEnabled).isTrue
        assertThat(pathConfiguration.properties("$url/new").pullToRefreshEnabled).isFalse
    }
}
