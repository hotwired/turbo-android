package com.basecamp.turbolinks

import android.os.Build
import com.basecamp.turbolinks.PresentationContext.DEFAULT
import com.basecamp.turbolinks.PresentationContext.MODAL
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class PathConfigurationTest {
    private val json = """
    {
      "rules": [
        {"patterns": [".+"], "properties": {"context": "default"} },
        {"patterns": ["/new$", "/edit$"], "properties": {"context": "modal"} }
      ]
    }
    """.trimIndent()

    private val pathConfiguration = PathConfiguration.load(json)

    @Test
    fun presentationContext() {
        assertThat(pathConfiguration.properties("/home").context).isEqualTo(DEFAULT)
        assertThat(pathConfiguration.properties("/new").context).isEqualTo(MODAL)
        assertThat(pathConfiguration.properties("/edit").context).isEqualTo(MODAL)
    }
}
