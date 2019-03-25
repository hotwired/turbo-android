package com.basecamp.turbolinks

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class RepositoryTest : BaseRepositoryTest() {
    private lateinit var context: Context
    private val repository = Repository()

    override fun setup() {
        super.setup()
        context = RuntimeEnvironment.application.applicationContext
        Http.sharedHttpClient = client()
    }

    @Test
    fun getRemoteConfiguration() {
        enqueueResponse("configuration.json")

        runBlocking {
            launch(Dispatchers.Main) {
                val json = repository.getRemoteConfiguration(baseUrl())
                assertThat(json).isNotNull()

                val config = json?.toObject(object : TypeToken<PathConfiguration>() {})
                assertThat(config?.rules?.size).isEqualTo(2)
            }
        }
    }

    @Test
    fun getBundledAssetConfiguration() {
        val json = repository.getBundledConfiguration(context, "json/configuration.json")
        assertThat(json).isNotNull()

        val config = load(json)
        assertThat(config?.rules?.size).isEqualTo(2)
    }

    @Test
    fun getCachedConfiguration() {
        cache(json())

        val json = repository.getCachedConfiguration(context)
        assertThat(json).isNotNull()

        val config = load(json)
        assertThat(config?.rules?.size).isEqualTo(1)
    }

    private fun load(json: String?): PathConfiguration? {
        return json?.toObject(object : TypeToken<PathConfiguration>() {})
    }

    private fun cache(json: String) {
        context.getSharedPreferences("turbolinks", Context.MODE_PRIVATE).edit(commit = true) {
            putString("configuration.json", json)
        }
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
