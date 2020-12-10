package dev.hotwire.turbo.config

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dev.hotwire.turbo.config.TurboPathConfiguration.Location
import dev.hotwire.turbo.nav.TurboNavPresentationContext
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
class TurboPathConfigurationTest {
    private lateinit var context: Context
    private lateinit var pathConfiguration: TurboPathConfiguration
    private val mockRepository = mock<TurboPathConfigurationRepository>()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pathConfiguration = TurboPathConfiguration(context).apply {
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

        assertThat(pathConfiguration.properties("$url/home").context).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(pathConfiguration.properties("$url/new").context).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(pathConfiguration.properties("$url/edit").context).isEqualTo(TurboNavPresentationContext.MODAL)
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
        assertThat(pathConfiguration.settings.size).isEqualTo(1)
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
