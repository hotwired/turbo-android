package dev.hotwire.turbo.delegates

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.R
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.TurboLog
import dev.hotwire.turbo.util.dispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext

internal const val TURBO_REQUEST_CODE_FILES = 37

internal class TurboFileUploadDelegate(val session: TurboSession) : CoroutineScope {
    private val context: Context = session.context
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImagePath: String? = null

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io + Job()

    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams
    ): Boolean {
        uploadCallback = filePathCallback

        return openChooser(params).also { success ->
            if (!success) handleCancellation()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        handleActivityResult(requestCode, resultCode, intent)
    }

    fun deleteCachedFiles() {
        launch {
            TurboFileProvider.deleteAllFiles(context)
        }
    }

    // Intents

    private fun openChooser(params: FileChooserParams): Boolean {
        val cameraIntent = buildCameraIntent(params)
        val chooserIntent = buildGetContentIntent(params)

        val mediaIntents = when (cameraIntent) {
            null -> emptyArray()
            else -> arrayOf(cameraIntent)
        }

        val intent = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, chooserIntent)
            putExtra(Intent.EXTRA_TITLE, params.title())
            putExtra(Intent.EXTRA_INITIAL_INTENTS, mediaIntents)
        }

        return startIntent(intent)
    }

    private fun buildGetContentIntent(params: FileChooserParams): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.allowsMultiple())
            type = params.defaultAcceptType()

            if (params.acceptTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, params.acceptTypes)
            }
        }
    }

    private fun buildCameraIntent(params: FileChooserParams): Intent? {
        if (!params.allowsCameraCapture()) return null

        return try {
            val file = createEmptyImageFile() ?: return null
            val uri = TurboFileProvider.contentUriForFile(session.context, file)

            cameraImagePath = file.absolutePath

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        } catch(e: Exception) {
            TurboLog.e("${e.message}")
            return null
        }
    }

    private fun startIntent(intent: Intent): Boolean {
        val destination = session.currentVisitNavDestination ?: return false

        return try {
            destination.fragment.startActivityForResult(intent, TURBO_REQUEST_CODE_FILES)
            true
        } catch (e: Exception) {
            TurboLog.e("${e.message}")
            false
        }
    }

    // Handle results

    private fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != TURBO_REQUEST_CODE_FILES) return

        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                handleCancellation()
            }
            Activity.RESULT_OK -> {
                if (intent?.dataString == null && intent?.clipData == null) {
                    handleCameraCapture()
                } else {
                    handleFileSelection(intent)
                }
            }
        }
    }

    private fun handleCancellation() {
        // Important to send a null value to the upload callback, otherwise the webview
        // gets into a state where it doesn't allow the file chooser to open again.
        uploadCallback?.onReceiveValue(null)
        uploadCallback = null
        cameraImagePath = null
    }

    private fun handleFileSelection(intent: Intent?) {
        if (intent == null) return

        launch {
            val clipData = intent.clipData
            val dataString = intent.dataString
            val results = when {
                clipData != null -> buildMultipleFilesResult(clipData)
                dataString != null -> buildSingleFileResult(dataString)
                else -> null
            }

            uploadCallback?.onReceiveValue(results)
            uploadCallback = null
        }
    }

    private fun handleCameraCapture() {
        val results = buildCameraImageResult()

        uploadCallback?.onReceiveValue(results)
        uploadCallback = null
        cameraImagePath = null
    }

    private suspend fun buildMultipleFilesResult(clipData: ClipData): Array<Uri>? {
        val arrayList = mutableListOf<Uri>()

        for (i in 0 until clipData.itemCount) {
            writeToCachedFile(clipData.getItemAt(i).uri)?.let {
                arrayList.add(it)
            }
        }

        return when (arrayList.isEmpty()) {
            true -> null
            else -> arrayList.toTypedArray()
        }
    }

    private suspend fun buildSingleFileResult(dataString: String): Array<Uri>? {
        val uri = writeToCachedFile(Uri.parse(dataString))
        return uri?.let { arrayOf(it) }
    }

    private fun buildCameraImageResult(): Array<Uri>? {
        val file = cameraImagePath?.let { File(it) } ?: return null
        val uri = TurboFileProvider.contentUriForFile(session.context, file)

        return when (file.length()) {
            0L -> null
            else -> arrayOf(uri)
        }
    }

    // Files

    private fun createEmptyImageFile(): File? {
        return try {
            val directory: File = TurboFileProvider.directory(session.context)
            return File.createTempFile("Capture_", ".jpg", directory)
        } catch (e: IOException) {
            TurboLog.e("${e.message}")
            null
        }
    }

    private suspend fun writeToCachedFile(uri: Uri): Uri? {
        return TurboFileProvider.writeUriToFile(context, uri)
    }

    // Params

    private fun FileChooserParams.allowsMultiple(): Boolean {
        return mode == FileChooserParams.MODE_OPEN_MULTIPLE
    }

    private fun FileChooserParams.defaultAcceptType(): String {
        return when {
            acceptTypes.isEmpty() -> "*/*"
            acceptTypes.first().isBlank() -> "*/*"
            else -> acceptTypes.first()
        }
    }

    private fun FileChooserParams.allowsCameraCapture(): Boolean {
        val accept = defaultAcceptType()
        val acceptsAny = accept == "*/*"
        val acceptsImages = accept == "image/*" || accept == "image/jpg"

        return isCaptureEnabled && (acceptsAny || acceptsImages)
    }

    private fun FileChooserParams.title(): String {
        return title?.toString() ?: when (allowsMultiple()) {
            true -> session.context.getString(R.string.turbo_file_chooser_select_multiple)
            else -> session.context.getString(R.string.turbo_file_chooser_select)
        }
    }
}
