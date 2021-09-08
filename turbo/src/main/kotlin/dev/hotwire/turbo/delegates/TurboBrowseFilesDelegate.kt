package dev.hotwire.turbo.delegates

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient.FileChooserParams
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.dispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class TurboBrowseFilesDelegate(val context: Context) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io + Job()

    fun buildIntent(params: FileChooserParams): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.allowsMultiple())
            type = params.defaultAcceptType()

            if (params.acceptTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, params.acceptTypes)
            }
        }
    }

    fun handleResult(intent: Intent?, onResult: (Array<Uri>?) -> Unit) {
        if (intent == null) {
            onResult(null)
            return
        }

        launch {
            val clipData = intent.clipData
            val dataString = intent.dataString
            val results = when {
                clipData != null -> buildMultipleFilesResult(clipData)
                dataString != null -> buildSingleFileResult(dataString)
                else -> null
            }

            onResult(results)
        }
    }

    private suspend fun buildMultipleFilesResult(clipData: ClipData): Array<Uri>? {
        val uris = mutableListOf<Uri>()

        for (i in 0 until clipData.itemCount) {
            uris.add(clipData.getItemAt(i).uri)
        }

        return buildResult(uris)
    }

    private suspend fun buildSingleFileResult(dataString: String): Array<Uri>? {
        val uri = Uri.parse(dataString)
        return buildResult(listOf(uri))
    }

    private suspend fun buildResult(uris: List<Uri>): Array<Uri>? {
        val results = uris.mapNotNull {
            writeToCachedFile(it)
        }

        return when (results.isEmpty()) {
            true -> null
            else -> results.toTypedArray()
        }
    }

    private suspend fun writeToCachedFile(uri: Uri): Uri? {
        return TurboFileProvider.writeUriToFile(context, uri)?.let {
            TurboFileProvider.contentUriForFile(context, it)
        }
    }
}
