package dev.hotwire.turbo.delegates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.TurboLog
import java.io.File
import java.io.IOException

internal class TurboCameraCaptureDelegate(val context: Context) {
    private var cameraImagePath: String? = null

    fun buildIntent(params: FileChooserParams): Intent? {
        if (!params.allowsCameraCapture()) return null

        val file = createEmptyImageFile() ?: return null
        val uri = TurboFileProvider.contentUriForFile(context, file)

        cameraImagePath = file.absolutePath

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
    }

    fun handleResult(onResult: (Array<Uri>?) -> Unit) {
        val results = buildCameraImageResult()

        onResult(results)
        cameraImagePath = null
    }

    private fun buildCameraImageResult(): Array<Uri>? {
        val file = cameraImagePath?.let { File(it) } ?: return null
        val uri = TurboFileProvider.contentUriForFile(context, file)

        return when (file.length()) {
            0L -> null
            else -> arrayOf(uri)
        }
    }

    private fun createEmptyImageFile(): File? {
        return try {
            val directory: File = TurboFileProvider.directory(context)
            return File.createTempFile("Capture_", ".jpg", directory)
        } catch (e: IOException) {
            TurboLog.e("${e.message}")
            null
        }
    }

    private fun FileChooserParams.allowsCameraCapture(): Boolean {
        val accept = defaultAcceptType()
        val acceptsAny = accept == "*/*"
        val acceptsImages = accept == "image/*" || accept == "image/jpg"

        return isCaptureEnabled && (acceptsAny || acceptsImages)
    }
}
