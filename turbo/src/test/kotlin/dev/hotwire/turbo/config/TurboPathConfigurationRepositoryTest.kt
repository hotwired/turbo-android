package dev.hotwire.turbo.config

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dev.hotwire.turbo.BaseRepositoryTest
import dev.hotwire.turbo.http.TurboHttpClient
import dev.hotwire.turbo.util.toObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboPathConfigurationRepositoryTest : BaseRepositoryTest() {
    private lateinit var context: Context
    private val repository = TurboPathConfigurationRepository()

    override fun setup() {
        super.setup()
        context = ApplicationProvider.getApplicationContext()
        TurboHttpClient.instance = client()
    }

    @Test
    fun getRemoteConfiguration() {
        enqueueResponse("test-configuration.json")

        runBlocking {
            launch(Dispatchers.Main) {
                val json = repository.getRemoteConfiguration(baseUrl())
                assertThat(json).isNotNull()

                val config = load(json)
                assertThat(config?.rules?.size).isEqualTo(2)
            }
        }
    }

    @Test
    fun getBundledAssetConfiguration() {
        val json = repository.getBundledConfiguration(context, "json/test-configuration.json")
        assertThat(json).isNotNull()

        val config = load(json)
        assertThat(config?.rules?.size).isEqualTo(8)
    }

    @Test
    fun getCachedConfiguration() {
        val url = "https://turbo.hotwired.dev/demo/configurations/android-v1.json"
        val config = requireNotNull(load(json()))
        repository.cacheConfigurationForUrl(context, url, config)

        val json = repository.getCachedConfigurationForUrl(context, url)
        assertThat(json).isNotNull()

        val cachedConfig = load(json)
        assertThat(cachedConfig?.rules?.size).isEqualTo(1)
    }

    private fun load(json: String?): TurboPathConfiguration? {
        return json?.toObject(object : TypeToken<TurboPathConfiguration>() {})
    }

    private fun json(): String {
        return """
        {
          "rules": [
            {"patterns": [".+"], "properties": {"context": "default"} }
          ]
        }
        """.trimIndent()
    }
}
