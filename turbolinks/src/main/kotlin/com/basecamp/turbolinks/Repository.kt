package com.basecamp.turbolinks

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

internal class Repository {
    private val cacheFile = "turbolinks"
    private val cacheKey = "configuration.json"

    suspend fun getRemoteConfiguration(url: String): String? {
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            issueRequest(request)
        }
    }

    fun getBundledConfiguration(context: Context, filePath: String): String {
        return contentFromAsset(context, filePath)
    }

    fun getCachedConfiguration(context: Context): String? {
        return prefs(context).getString(cacheKey, null)
    }

    fun cacheConfiguration(context: Context, pathConfiguration: PathConfiguration) {
        prefs(context).edit {
            putString(cacheKey, pathConfiguration.toJson())
        }
    }

    private fun issueRequest(request: Request): String? = try {
        Http.sharedHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                logError(request, response.code.toString())
                null
            }
        }
    } catch (e: IOException) {
        logError(request, e.message)
        null
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(cacheFile, Context.MODE_PRIVATE)
    }

    private fun contentFromAsset(context: Context, filePath: String): String {
        return context.assets.open(filePath).use {
            String(it.readBytes())
        }
    }

    private fun logError(request: Request, message: String?) {
        TurbolinksLog.e("Response failed for ${request.url} : $message")
    }
}
