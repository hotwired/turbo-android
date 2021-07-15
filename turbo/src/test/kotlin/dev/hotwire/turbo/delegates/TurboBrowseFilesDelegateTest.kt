package dev.hotwire.turbo.delegates

import android.content.Context
import android.content.Intent
import android.os.Build
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN
import android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
import androidx.test.core.app.ApplicationProvider
import dev.hotwire.turbo.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboBrowseFilesDelegateTest : BaseUnitTest() {
    private lateinit var context: Context
    private lateinit var delegate: TurboBrowseFilesDelegate

    @Before
    override fun setup() {
        super.setup()
        context = ApplicationProvider.getApplicationContext()
        delegate = TurboBrowseFilesDelegate(context)
    }

    @Test
    fun buildIntent() {
        val intent = delegate.buildIntent(params())

        assertThat(intent.categories.first()).isEqualTo(Intent.CATEGORY_OPENABLE)
        assertThat(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)).isFalse
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)).isNull()
        assertThat(intent.type).isEqualTo("*/*")
    }

    @Test
    fun buildIntentMultiple() {
        val intent = delegate.buildIntent(params(mode = MODE_OPEN_MULTIPLE))
        assertThat(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)).isTrue
    }

    @Test
    fun buildIntentAcceptType() {
        val intent = delegate.buildIntent(params(acceptTypes = arrayOf("image/*")))
        assertThat(intent.type).isEqualTo("image/*")
    }

    @Test
    fun buildIntentAcceptTypeMultiple() {
        val acceptTypes = arrayOf("image/*", "video/*")
        val intent = delegate.buildIntent(params(acceptTypes = acceptTypes))

        assertThat(intent.type).isEqualTo("image/*")
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)).isEqualTo(acceptTypes)
    }

    @Test
    fun buildIntentAcceptTypeEmpty() {
        val intent = delegate.buildIntent(params(acceptTypes = arrayOf()))
        assertThat(intent.type).isEqualTo("*/*")
    }

    private fun params(
        mode: Int = MODE_OPEN,
        acceptTypes: Array<String> = arrayOf("*/*")
    ): FileChooserParams {
        return object : FileChooserParams() {
            override fun getMode() = mode
            override fun getAcceptTypes() = acceptTypes
            override fun isCaptureEnabled() = false
            override fun getTitle() = "title"
            override fun getFilenameHint() = "hint"
            override fun createIntent() = Intent()
        }
    }
}
