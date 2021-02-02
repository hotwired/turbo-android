package dev.hotwire.turbo.views

import android.content.Context
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.hotwire.turbo.R
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.views.TurboWebChromeClient.FileChooserType.BROWSE
import dev.hotwire.turbo.views.TurboWebChromeClient.FileChooserType.CAMERA

@Suppress("MemberVisibilityCanBePrivate")
open class TurboWebChromeClient(val session: TurboSession) : WebChromeClient() {
    enum class FileChooserType {
        BROWSE, CAMERA
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams
    ): Boolean {
        val types = mutableListOf(BROWSE)

        if (params.allowsCameraCapture()) {
            types.add(CAMERA)
        }

        return when (types.size) {
            1 -> showFileChooser(filePathCallback, params, types.first())
            else -> displayFileChooserPicker(filePathCallback, params, types)
        }
    }

    protected fun showFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams,
        type: FileChooserType
    ): Boolean {
        return session.fileUploadDelegate.onShowFileChooser(
            filePathCallback = filePathCallback,
            params = params,
            type = type
        )
    }

    protected fun cancelFileChooser(filePathCallback: ValueCallback<Array<Uri>>) {
        session.fileUploadDelegate.onShowFileChooserCancelled(filePathCallback)
    }

    private fun displayFileChooserPicker(
        filePathCallback: ValueCallback<Array<Uri>>,
        params: FileChooserParams,
        types: List<FileChooserType>
    ): Boolean {
        val destination = session.currentVisitNavDestination ?: return false
        val context = destination.fragment.context ?: return false
        val title = params.dialogTitle(context)
        val descriptions = types.map { it.description(context) }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setItems(descriptions.toTypedArray()) { _, selection ->
                showFileChooser(filePathCallback, params, types[selection])
            }
            .setOnCancelListener {
                cancelFileChooser(filePathCallback)
            }
            .create()
            .show()

        return true
    }

    private fun FileChooserParams.allowsCameraCapture(): Boolean {
        val defaultAcceptType = session.fileUploadDelegate.defaultAcceptType(this)
        val acceptsImages = defaultAcceptType == "*/*" || defaultAcceptType.startsWith("image/")

        return isCaptureEnabled && acceptsImages
    }

    private fun FileChooserParams.dialogTitle(context: Context): String {
        return title?.toString() ?: context.getString(R.string.turbo_file_chooser_select)
    }

    private fun FileChooserType.description(context: Context): String {
        return when (this) {
            BROWSE -> context.getString(R.string.turbo_file_chooser_browse)
            CAMERA -> context.getString(R.string.turbo_file_chooser_camera)
        }
    }
}
