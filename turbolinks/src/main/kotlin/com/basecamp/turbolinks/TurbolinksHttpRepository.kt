package com.basecamp.turbolinks

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.basecamp.turbolinks.OfflineCacheStrategy.APP
import com.basecamp.turbolinks.OfflineCacheStrategy.NONE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

enum class OfflineCacheStrategy {
    APP, NONE
}

@Suppress("unused")
enum class OfflineCacheControl {
    STALE_IF_ERROR, IMMUTABLE
}

interface TurbolinksOfflineRequestHandler {
    fun getCacheStrategy(url: String): OfflineCacheStrategy
    fun getCacheControl(url: String): OfflineCacheControl
    fun getCachedSnapshot(url: String): WebResourceResponse?
    fun getCachedResponse(url: String, allowStaleResponse: Boolean = false): WebResourceResponse?
    fun cacheResponse(url: String, response: WebResourceResponse)
}

internal class TurbolinksHttpRepository {
    private val cookieManager = CookieManager.getInstance()

    private class HttpResponse(
        val response: Response,
        val responseBody: ByteArray?
    )

    data class Result(
        val response: WebResourceResponse?,
        val offline: Boolean
    )

    internal suspend fun preCache(requestHandler: TurbolinksOfflineRequestHandler,
                                  resourceRequest: WebResourceRequest) {
        withContext(Dispatchers.IO) {
            fetch(requestHandler, resourceRequest)
        }
    }

    internal fun fetch(requestHandler: TurbolinksOfflineRequestHandler,
                       resourceRequest: WebResourceRequest): Result {
        val url = resourceRequest.url.toString()

        return when (requestHandler.getCacheStrategy(url)) {
            APP -> fetchAppCacheRequest(requestHandler, resourceRequest)
            NONE -> Result(null, false)
        }
    }

    private fun fetchAppCacheRequest(requestHandler: TurbolinksOfflineRequestHandler,
                                     resourceRequest: WebResourceRequest): Result {
        val url = resourceRequest.url.toString()
        val cacheControl = requestHandler.getCacheControl(url)

        // If the app has an immutable response cached, don't hit the network
        if (cacheControl == OfflineCacheControl.IMMUTABLE) {
            requestHandler.getCachedResponse(url)?.let {
                return Result(it, false)
            }
        }

        return try {
            val response = issueRequest(resourceRequest)

            // Let the app cache the response
            resourceResponse(response)?.let {
                requestHandler.cacheResponse(url, it)
            }

            Result(resourceResponse(response), false)
        } catch (e: IOException) {
            Result(requestHandler.getCachedResponse(url, allowStaleResponse = true), true)
        }
    }

    private fun issueRequest(resourceRequest: WebResourceRequest): HttpResponse? {
        return try {
            val request = buildRequest(resourceRequest)
            getResponse(request)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            TurbolinksLog.e("Request error: ${e.message}")
            null
        }
    }

    private fun buildRequest(resourceRequest: WebResourceRequest): Request {
        val location = resourceRequest.url.toString()
        val headers = resourceRequest.requestHeaders
        val builder = Request.Builder().url(location)

        headers.forEach { builder.header(it.key, it.value) }

        getCookie(location)?.let {
            builder.header("Cookie", it)
        }

        return builder.build()
    }

    private fun getResponse(request: Request): HttpResponse? {
        val location = request.url.toString()
        val call = TurbolinksHttpClient.instance.newCall(request)

        return call.execute().use { response ->
            if (response.isSuccessful) {
                setCookies(location, response)
                HttpResponse(response, response.body?.bytes())
            } else {
                null
            }
        }
    }

    private fun getCookie(location: String): String? {
        return cookieManager.getCookie(location)
    }

    private fun setCookies(location: String, response: Response) {
        response.headers("Set-Cookie").forEach {
            cookieManager.setCookie(location, it)
        }
    }

    private fun resourceResponse(response: HttpResponse?): WebResourceResponse? {
        if (response == null) {
            return null
        }

        return WebResourceResponse(
            mimeType(response.response),
            encoding(),
            statusCode(response.response),
            reasonPhrase(response.response),
            responseHeaders(response.response),
            data(response.responseBody)
        )
    }

    private fun mimeType(response: Response): String {
        // A Content-Type header may not exist, provide a fallback.
        return when (val contentType = response.headers["Content-Type"]) {
            null -> "text/plain"
            else -> sanitizeContentType(contentType)
        }
    }

    private fun sanitizeContentType(contentType: String): String {
        // The Content-Type header may contain a charset suffix,
        // but this is incompatible with a WebResourceResponse and
        // the resource will default to `text/plain` otherwise.
        return contentType.removeSuffix("; charset=utf-8")
    }

    private fun encoding(): String {
        return "utf-8"
    }

    private fun statusCode(response: Response): Int {
        return response.code
    }

    private fun reasonPhrase(response: Response): String {
        // A reason phrase cannot be empty
        return when (response.message.isEmpty()) {
            true -> "OK"
            else -> response.message
        }
    }

    private fun responseHeaders(response: Response): Map<String, String> {
        return response.headers.toMap()
    }

    private fun data(responseBody: ByteArray?): InputStream? {
        return try {
            ByteArrayInputStream(responseBody)
        } catch (e: Exception) {
            TurbolinksLog.e("Byte stream error: ${e.message}")
            null
        }
    }
}
