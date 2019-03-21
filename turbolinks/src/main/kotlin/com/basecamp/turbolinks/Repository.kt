package com.basecamp.turbolinks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

internal class Repository {
    private val client = OkHttpClient()

    suspend fun getRemotePathConfiguration(url: String): PathConfiguration? {
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            issueRequest(request)?.let {
                PathConfiguration.load(it)
            }
        }
    }

    private fun issueRequest(request: Request): String? = try {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body()?.string()
            } else {
                logError(request, response.code().toString())
                null
            }
        }
    } catch (e: IOException) {
        logError(request, e.message)
        null
    }

    private fun logError(request: Request, message: String?) {
        TurbolinksLog.e("Response failed for ${request.url()} : $message")
    }
}
