package com.basecamp.turbolinks

import android.os.Build
import com.basecamp.turbolinks.PresentationContext.DEFAULT
import com.basecamp.turbolinks.PresentationContext.MODAL
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
class RepositoryTest : BaseRepositoryTest() {
    private val repository = Repository()

    override fun setup() {
        super.setup()
        Http.sharedHttpClient = client()
    }

    @Test
    fun getRemotePathConfiguration() {
        enqueueResponse("configuration.json")

        runBlocking {
            launch(Dispatchers.Main) {
                val config = repository.getRemotePathConfiguration(baseUrl())
                assertThat(config).isNotNull
                assertThat(config?.rules?.size).isEqualTo(2)
                assertThat(config?.properties("/home")?.context).isEqualTo(DEFAULT)
                assertThat(config?.properties("/new")?.context).isEqualTo(MODAL)
                assertThat(config?.properties("/edit")?.context).isEqualTo(MODAL)
            }
        }
    }
}
