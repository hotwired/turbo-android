package dev.hotwire.turbo.delegates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.WebChromeClient.FileChooserParams
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
class TurboCameraCaptureDelegateTest : BaseUnitTest() {
    private lateinit var context: Context
    private lateinit var delegate: TurboCameraCaptureDelegate

    @Before
    override fun setup() {
        super.setup()
        context = ApplicationProvider.getApplicationContext()
        delegate = TurboCameraCaptureDelegate(context)
    }

    @Test
    fun buildIntentAcceptTypesValid() {
        val acceptTypes = arrayOf(
            "*/*",
            "image/*",
            "image/jpg",
            "image/jpeg"
        )

        acceptTypes.forEach {
            val intent = delegate.buildIntent(params(acceptTypes = arrayOf(it)))
            val uri = intent?.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT).toString()

            assertThat(intent).isNotNull()
            assertThat(intent?.action).isEqualTo(MediaStore.ACTION_IMAGE_CAPTURE)
            assertThat(uri).startsWith("content://dev.hotwire.turbo.test.turbo.fileprovider/shared")
            assertThat(uri).contains("/Capture_")
            assertThat(uri).endsWith(".jpg")
        }
    }

    @Test
    fun buildIntentAcceptTypesInvalid() {
        val acceptTypes = arrayOf(
            "image/png",
            "image/webp",
            "video/*"
        )

        acceptTypes.forEach {
            val intent = delegate.buildIntent(params(acceptTypes = arrayOf(it)))
            assertThat(intent).isNull()
        }
    }

    @Test
    fun buildIntentCaptureDisabled() {
        val intent = delegate.buildIntent(params(captureEnabled = false))
        assertThat(intent).isNull()
    }

    private fun params(
        acceptTypes: Array<String> = arrayOf("*/*"),
        captureEnabled: Boolean = true
    ): FileChooserParams {
        return object : FileChooserParams() {
            override fun getMode() = MODE_OPEN
            override fun getAcceptTypes() = acceptTypes
            override fun isCaptureEnabled() = captureEnabled
            override fun getTitle() = "title"
            override fun getFilenameHint() = "hint"
            override fun createIntent() = Intent()
        }
    }
}
