package dev.hotwire.turbo.config

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dev.hotwire.turbo.config.TurboPathConfiguration.Location
import dev.hotwire.turbo.nav.TurboNavPresentationContext
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import dev.hotwire.turbo.BaseRepositoryTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboPathConfigurationTest : BaseRepositoryTest() {
    private lateinit var context: Context
    private lateinit var pathConfiguration: TurboPathConfiguration
    private val mockRepository = mock<TurboPathConfigurationRepository>()
    private val url = "https://turbo.hotwired.dev"

    @Before
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext()
        pathConfiguration = TurboPathConfiguration(context).apply {
            load(Location(assetFilePath = "json/test-configuration.json"))
        }
    }

    @Test
    fun assetConfigurationIsLoaded() {
        assertThat(pathConfiguration.rules.size).isEqualTo(8)
    }

    @Test
    fun presentationContext() {
        assertThat(pathConfiguration.properties("$url/home").context).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(pathConfiguration.properties("$url/new").context).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(pathConfiguration.properties("$url/edit").context).isEqualTo(TurboNavPresentationContext.MODAL)
    }

    @Test
    fun remoteConfigurationIsFetched() {
        pathConfiguration.loader.repository = mockRepository

        runBlocking {
            val remoteUrl = "$url/demo/configurations/android-v1.json"
            val location = Location(remoteFileUrl = remoteUrl)

            pathConfiguration.load(location)
            verify(mockRepository).getCachedConfigurationForUrl(context, remoteUrl)
            verify(mockRepository).getRemoteConfiguration(remoteUrl)
        }
    }

    @Test
    fun globalSetting() {
        assertThat(pathConfiguration.settings.size).isEqualTo(1)
        assertThat(pathConfiguration.settings["custom_app_feature_enabled"]).isEqualTo("true")
        assertThat(pathConfiguration.settings["no_such_key"]).isNull()
    }

    @Test
    fun title() {
        assertThat(pathConfiguration.properties("$url/image.jpg").title).isEqualTo("Image Viewer")
    }

    @Test
    fun pullToRefresh() {
        assertThat(pathConfiguration.properties("$url/home").pullToRefreshEnabled).isTrue
        assertThat(pathConfiguration.properties("$url/new").pullToRefreshEnabled).isFalse
    }
}
