package dev.hotwire.turbo.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

@Suppress("MemberVisibilityCanBePrivate")
class TurboFileProvider : FileProvider() {
    companion object {
        fun authority(context: Context): String {
            return "${context.packageName}.turbo.fileprovider"
        }

        fun directory(context: Context): File {
            val directory = File(context.filesDir, "shared")

            if (!directory.mkdirs() && !directory.isDirectory) {
                throw IOException("Could not create file provider directory")
            }

            return directory
        }

        fun contentUriForFile(context: Context, file: File): Uri {
            return getUriForFile(context, authority(context), file)
        }

        suspend fun writeUriToFile(context: Context, uri: Uri): Uri? {
            val uriHelper = TurboUriHelper(context)

            return uriHelper.writeFileTo(uri, directory(context))?.let {
                contentUriForFile(context, it)
            }
        }

        suspend fun deleteAllFiles(context: Context) {
            withContext(dispatcherProvider.io) {
                directory(context).deleteAllFilesInDirectory()
            }
        }
    }
}
