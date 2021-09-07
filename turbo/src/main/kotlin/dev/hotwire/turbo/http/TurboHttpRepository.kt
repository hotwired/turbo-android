package dev.hotwire.turbo.http

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import dev.hotwire.turbo.util.TurboLog
import dev.hotwire.turbo.util.dispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

/**
 * Experimental: API may change, not ready for production use.
 */
internal class TurboHttpRepository(private val coroutineScope: CoroutineScope) {
    private val cookieManager = CookieManager.getInstance()

    // Limit pre-cache requests to 2 concurrently
    private val preCacheRequestQueue = Semaphore(2)

    data class Result(
        val response: WebResourceResponse?,
        val offline: Boolean,
        val redirectToLocation: String? = null
    )

    internal fun preCache(
        requestHandler: TurboOfflineRequestHandler,
        resourceRequest: WebResourceRequest
    ) {
        coroutineScope.launch {
            preCacheRequestQueue.withPermit {
                withContext(dispatcherProvider.io) {
                    fetch(requestHandler, resourceRequest)
                }
            }
        }
    }

    internal fun fetch(
        requestHandler: TurboOfflineRequestHandler,
        resourceRequest: WebResourceRequest
    ): Result {
        val url = resourceRequest.url.toString()

        return when (requestHandler.getCacheStrategy(url)) {
            TurboOfflineCacheStrategy.APP -> fetchAppCacheRequest(requestHandler, resourceRequest)
            TurboOfflineCacheStrategy.NONE -> Result(null, false)
        }
    }

    private fun fetchAppCacheRequest(
        requestHandler: TurboOfflineRequestHandler,
        resourceRequest: WebResourceRequest
    ): Result {
        val url = resourceRequest.url.toString()
        val headers = requestHandler.getCachedResponseHeaders(url) ?: emptyMap()
        val cacheControl = cacheControl(headers)

        // If the app has an immutable response cached, don't hit the network
        if (cacheControl.immutable) {
            requestHandler.getCachedResponse(url)?.let {
                return Result(it, false)
            }
        }

        return try {
            val response = issueRequest(resourceRequest)

            // Cache based on the response's request url, which may have been a redirect
            val responseUrl = response?.request?.url.toString()
            val isRedirect = url != responseUrl

            // Let the app cache the response
            val resourceResponse = resourceResponse(response)
            val cachedResponse = resourceResponse?.let {
                requestHandler.cacheResponse(responseUrl, it)
            }

            Result(
                response = cachedResponse ?: resourceResponse,
                offline = false,
                redirectToLocation = if (isRedirect) responseUrl else null
            )
        } catch (e: IOException) {
            Result(
                response = requestHandler.getCachedResponse(url, allowStaleResponse = true),
                offline = true
            )
        }
    }

    private fun issueRequest(resourceRequest: WebResourceRequest): Response? {
        return try {
            val request = buildRequest(resourceRequest)
            getResponse(request)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            TurboLog.e("Request error: ${e.message}")
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

    private fun getResponse(request: Request): Response? {
        val location = request.url.toString()
        val call = TurboHttpClient.instance.newCall(request)

        return call.execute().let { response ->
            if (response.isSuccessful) {
                setCookies(location, response)
                response
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

    private fun resourceResponse(response: Response?): WebResourceResponse? {
        if (response == null) {
            return null
        }

        return WebResourceResponse(
            mimeType(response),
            encoding(),
            statusCode(response),
            reasonPhrase(response),
            responseHeaders(response),
            data(response)
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
        return when (response.message.isBlank()) {
            true -> "OK"
            else -> response.message
        }
    }

    private fun responseHeaders(response: Response): Map<String, String> {
        return response.headers.toMap()
    }

    private fun cacheControl(headers: Map<String, String>): CacheControl {
        return try {
            CacheControl.parse(headers.toHeaders())
        } catch (e: Exception) {
            // Bad header characters can cause the parser to fail
            CacheControl.parse(emptyMap<String, String>().toHeaders())
        }
    }

    private fun data(response: Response?): InputStream? {
        return try {
            response?.body?.byteStream()
        } catch (e: Exception) {
            TurboLog.e("Byte stream error: ${e.message}")
            null
        }
    }
}
