package com.basecamp.turbolinks

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.basecamp.turbolinks.PathConfiguration.Location
import com.basecamp.turbolinks.TurbolinksNavigationRule.PresentationContext
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
class PathConfigurationTest {
    private lateinit var context: Context
    private lateinit var pathConfiguration: PathConfiguration
    private val mockRepository = mock<PathConfigurationRepository>()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pathConfiguration = PathConfiguration(context).apply {
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

        assertThat(pathConfiguration.properties("$url/home").context).isEqualTo(PresentationContext.DEFAULT)
        assertThat(pathConfiguration.properties("$url/new").context).isEqualTo(PresentationContext.MODAL)
        assertThat(pathConfiguration.properties("$url/edit").context).isEqualTo(PresentationContext.MODAL)
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
}
