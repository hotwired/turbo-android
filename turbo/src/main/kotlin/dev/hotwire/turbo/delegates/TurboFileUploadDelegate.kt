package dev.hotwire.turbo.delegates

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.dispatcherProvider
import dev.hotwire.turbo.views.TurboWebChromeClient
import dev.hotwire.turbo.views.TurboWebChromeClient.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal const val TURBO_REQUEST_CODE_FILES = 37

internal class TurboFileUploadDelegate(val session: TurboSession) : CoroutineScope {
    private val context: Context = session.context
    private var uploadCallback: ValueCallback<Array<Uri>>? = null

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
        }
    }

    fun onShowFileChooserCancelled(filePathCallback: ValueCallback<Array<Uri>>) {
        uploadCallback = filePathCallback
        handleFileCancellation()
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

    private fun openFileChooser(params: FileChooserParams): Boolean {
        val destination = session.currentVisitNavDestination ?: return false
        val allowMultiple = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
        val intent = buildGetContentIntent(params, allowMultiple)
        val title = params.title ?: when (allowMultiple) {
            true -> "Choose Files"
            else -> "Choose File"
        }

        val chooserIntent = Intent.createChooser(intent, title)
        destination.fragment.startActivityForResult(chooserIntent, TURBO_REQUEST_CODE_FILES)
        return true
    }

    private fun openCamera(): Boolean {
        // TODO
        handleFileCancellation()
        return false
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

    private fun buildCameraIntent(): Intent {
        TODO("not implemented")
    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != TURBO_REQUEST_CODE_FILES) return

        when (resultCode) {
            Activity.RESULT_CANCELED -> handleFileCancellation()
            Activity.RESULT_OK -> handleFileSelection(intent)
        }
    }

    private fun handleFileCancellation() {
        // Important to send a null value to the upload callback, otherwise the webview
        // gets into a state where it doesn't allow the file chooser to open again.
        uploadCallback?.onReceiveValue(null)
        uploadCallback = null
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

    private suspend fun writeToCachedFile(uri: Uri): Uri? {
        return TurboFileProvider.writeUriToFile(context, uri)
    }
}
