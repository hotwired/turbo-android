package dev.hotwire.turbo.delegates

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.TurboLog
import dev.hotwire.turbo.util.dispatcherProvider
import dev.hotwire.turbo.views.TurboWebChromeClient.FileChooserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

internal const val TURBO_REQUEST_CODE_FILES = 37
internal const val TURBO_REQUEST_CODE_CAMERA = 38

internal class TurboFileUploadDelegate(val session: TurboSession) : CoroutineScope {
    private val context: Context = session.context
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImagePath: String? = null

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io + Job()

    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams,
        type: FileChooserType
    ): Boolean {
        uploadCallback = filePathCallback

        return when (type) {
            FileChooserType.BROWSE -> openFileChooser(params)
            FileChooserType.CAMERA -> openCamera()
        }.also { success ->
            if (!success) handleCancellation()
        }
    }

    fun onShowFileChooserCancelled(filePathCallback: ValueCallback<Array<Uri>>) {
        uploadCallback = filePathCallback
        handleCancellation()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        handleActivityResult(requestCode, resultCode, intent)
    }

    fun deleteCachedFiles() {
        launch {
            TurboFileProvider.deleteAllFiles(context)
        }
    }

    fun defaultAcceptType(params: FileChooserParams): String {
        return when {
            params.acceptTypes.isEmpty() -> "*/*"
            params.acceptTypes.first().isBlank() -> "*/*"
            else -> params.acceptTypes.first()
        }
    }

    // Intents

    private fun openFileChooser(params: FileChooserParams): Boolean {
        val allowMultiple = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
        val intent = buildGetContentIntent(params, allowMultiple)
        val title = params.title ?: when (allowMultiple) {
            true -> "Choose Files"
            else -> "Choose File"
        }

        val chooserIntent = Intent.createChooser(intent, title)
        return startIntent(chooserIntent, TURBO_REQUEST_CODE_FILES)
    }

    private fun openCamera(): Boolean {
        val intent = buildCameraIntent() ?: return false
        return startIntent(intent, TURBO_REQUEST_CODE_CAMERA)
    }

    private fun buildGetContentIntent(params: FileChooserParams, allowMultiple: Boolean): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            type = defaultAcceptType(params)

            if (params.acceptTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, params.acceptTypes)
            }
        }
    }

    private fun buildCameraIntent(): Intent? {
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

    private fun startIntent(intent: Intent, requestCode: Int): Boolean {
        val destination = session.currentVisitNavDestination ?: return false

        return try {
            destination.fragment.startActivityForResult(intent, requestCode)
            true
        } catch (e: Exception) {
            TurboLog.e("${e.message}")
            false
        }
    }

    // Handle results

    private fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == TURBO_REQUEST_CODE_FILES) {
            when (resultCode) {
                Activity.RESULT_CANCELED -> handleCancellation()
                Activity.RESULT_OK -> handleFileSelection(intent)
            }
        } else if (requestCode == TURBO_REQUEST_CODE_CAMERA) {
            when (resultCode) {
                Activity.RESULT_CANCELED -> handleCancellation()
                Activity.RESULT_OK -> handleCameraCapture()
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

    @SuppressLint("SimpleDateFormat")
    private fun createEmptyImageFile(): File? {
        return try {
            val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val directory: File = TurboFileProvider.directory(session.context)
            return File.createTempFile("capture_${timestamp}", ".jpg", directory)
        } catch (e: IOException) {
            TurboLog.e("${e.message}")
            null
        }
    }

    private suspend fun writeToCachedFile(uri: Uri): Uri? {
        return TurboFileProvider.writeUriToFile(context, uri)
    }
}
