package dev.hotwire.turbo.config

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
import dev.hotwire.turbo.http.TurboHttpClient
import dev.hotwire.turbo.util.dispatcherProvider
import dev.hotwire.turbo.util.logError
import dev.hotwire.turbo.util.toJson
import dev.hotwire.turbo.util.toObject
import kotlinx.coroutines.withContext
import okhttp3.Request

internal class TurboPathConfigurationRepository {
    private val cacheFile = "turbo"

    suspend fun getRemoteConfiguration(url: String): TurboPathConfiguration? {
        val request = Request.Builder().url(url).build()

        return withContext(dispatcherProvider.io) {
            issueRequest(request)?.let { parseFromJson(it) }
        }
    }

    fun getBundledConfiguration(context: Context, filePath: String): TurboPathConfiguration? {
        val bundledConfigJson = contentFromAsset(context, filePath)
        return parseFromJson(bundledConfigJson)
    }

    fun getCachedConfigurationForUrl(context: Context, url: String): TurboPathConfiguration? {
        val cachedConfigJson = prefs(context).getString(url, null)
        return cachedConfigJson?.let { parseFromJson(it) }
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
                    logError(
                        "remotePathConfigurationFailure",
                        Exception("location: ${request.url}, status code: ${response.code}")
                    )
                    null
                }
            }
        } catch (e: Exception) {
            logError("remotePathConfigurationException", e)
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

    @VisibleForTesting
    fun parseFromJson(json: String): TurboPathConfiguration? {
        return try {
            json.toObject(object : TypeToken<TurboPathConfiguration>() {})
        } catch (e: Exception) {
            logError("PathConfigurationLoadingException", e)
            null
        }
    }
}
