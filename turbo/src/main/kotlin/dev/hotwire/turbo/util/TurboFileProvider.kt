package dev.hotwire.turbo.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class TurboFileProvider : FileProvider() {
    companion object {
        private const val sharedDir = "shared"

        fun authority(context: Context): String {
            return "${context.packageName}.turbo.fileprovider"
        }

        fun directory(context: Context, dirName: String = sharedDir): File {
            val directory = File(context.filesDir, dirName)

            if (!directory.mkdirs() && !directory.isDirectory) {
                throw IOException("Could not create file provider directory")
            }

            return directory
        }

        fun contentUriForFile(context: Context, file: File): Uri {
            return getUriForFile(context, authority(context), file)
        }

        @Suppress("unused")
        fun uriAttributes(context: Context, uri: Uri): TurboUriAttributes? {
            val uriHelper = TurboUriHelper(context)
            return uriHelper.getAttributes(uri)
        }

        suspend fun writeUriToFile(context: Context, uri: Uri, dirName: String = sharedDir): File? {
            val uriHelper = TurboUriHelper(context)
            return uriHelper.writeFileTo(uri, directory(context, dirName))
        }

        suspend fun deleteAllFiles(context: Context, dirName: String = sharedDir) {
            withContext(dispatcherProvider.io) {
                directory(context, dirName).deleteAllFilesInDirectory()
            }
        }
    }
}
