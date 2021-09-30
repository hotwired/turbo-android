package dev.hotwire.turbo.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.hotwire.turbo.http.TurboHttpClient
import dev.hotwire.turbo.util.dispatcherProvider
import dev.hotwire.turbo.util.toJson
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

internal class TurboPathConfigurationRepository {
    private val cacheFile = "turbo"

    suspend fun getRemoteConfiguration(url: String): String? {
        val request = Request.Builder().url(url).build()

        return withContext(dispatcherProvider.io) {
            issueRequest(request)
        }
    }

    fun getBundledConfiguration(context: Context, filePath: String): String {
        return contentFromAsset(context, filePath)
    }

    fun getCachedConfigurationForUrl(context: Context, url: String): String? {
        return prefs(context).getString(url, null)
    }

    fun cacheConfigurationForUrl(context: Context, url: String, pathConfiguration: TurboPathConfiguration) {
        prefs(context).edit {
            putString(url, pathConfiguration.toJson())
        }
    }

    private fun issueRequest(request: Request): String? {
        return try {
            val call = TurboHttpClient.instance.newCall(request)

            call.execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(cacheFile, Context.MODE_PRIVATE)
    }

    private fun contentFromAsset(context: Context, filePath: String): String {
        return context.assets.open(filePath).use {
            String(it.readBytes())
        }
    }
}
