package com.basecamp.turbolinks

import android.content.Context
import android.os.Build
import com.basecamp.turbolinks.PathConfiguration.Location
import com.basecamp.turbolinks.PresentationContext.DEFAULT
import com.basecamp.turbolinks.PresentationContext.MODAL
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class PathConfigurationTest {
    private lateinit var context: Context
    private lateinit var pathConfiguration: PathConfiguration
    private val mockRepository = mock<Repository>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.application.applicationContext
        pathConfiguration = PathConfiguration(context).apply {
            load(Location(assetFilePath = "json/configuration.json"))
        }
    }

    @Test
    fun assetConfigurationIsLoaded() {
        assertThat(pathConfiguration.rules.size).isEqualTo(2)
    }

    @Test
    fun presentationContext() {
        assertThat(pathConfiguration.properties("/home").context).isEqualTo(DEFAULT)
        assertThat(pathConfiguration.properties("/new").context).isEqualTo(MODAL)
        assertThat(pathConfiguration.properties("/edit").context).isEqualTo(MODAL)
    }

    @Test
    fun remoteConfigurationIsFetched() {
        pathConfiguration.loader.repository = mockRepository

        runBlocking {
            val url = "http://example.com/configuration.json"
            val location = Location(remoteFileUrl = url)

            pathConfiguration.load(location)
            verify(mockRepository).getCachedConfiguration(context)
            verify(mockRepository).getRemoteConfiguration(url)
        }
    }
}
