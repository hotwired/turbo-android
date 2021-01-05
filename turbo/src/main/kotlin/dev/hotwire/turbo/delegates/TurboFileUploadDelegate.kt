package dev.hotwire.turbo.delegates

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.session.TurboSession

const val TURBO_REQUEST_CODE_FILES = 37

class TurboFileUploadDelegate(val session: TurboSession) {
    private var uploadCallback: ValueCallback<Array<Uri>>? = null

    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams,
        destination: TurboNavDestination
    ): Boolean {
        uploadCallback = filePathCallback
        openFileChooser(params, destination)
        return true
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        handleActivityResult(requestCode, resultCode, intent)
    }

    private fun openFileChooser(params: FileChooserParams, destination: TurboNavDestination) {
        val allowMultiple = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE

        val fileTypesIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)

            if (params.acceptTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, params.acceptTypes)
            }

            type = when {
                params.acceptTypes.isEmpty() -> "*/*"
                params.acceptTypes.first().isBlank() -> "*/*"
                else -> params.acceptTypes.first()
            }
        }

        val title = params.title ?: when (allowMultiple) {
            true -> "Choose Files"
            else -> "Choose File"
        }

        val chooserIntent = Intent.createChooser(fileTypesIntent, title)
        destination.fragment.startActivityForResult(chooserIntent, TURBO_REQUEST_CODE_FILES)
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

    private fun buildMultipleFilesResult(clipData: ClipData): Array<Uri>? {
        val arrayList = mutableListOf<Uri>()

        for (i in 0 until clipData.itemCount) {
            arrayList.add(clipData.getItemAt(i).uri)
        }

        return when {
            arrayList.isEmpty() -> null
            else -> arrayList.toTypedArray()
        }
    }

    private fun buildSingleFileResult(dataString: String): Array<Uri> {
        return arrayOf(Uri.parse(dataString))
    }
}
