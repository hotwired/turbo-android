package com.basecamp.turbolinks

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class TurbolinksHttpRepository {
    private val cookieManager = CookieManager.getInstance()

    fun fetch(resourceRequest: WebResourceRequest?): WebResourceResponse? {
        resourceRequest ?: return null

        val request = buildRequest(resourceRequest)
        return issueRequest(request)
    }

    private fun buildRequest(resourceRequest: WebResourceRequest): Request {
        val url = resourceRequest.url.toString()
        val headers = resourceRequest.requestHeaders
        val builder = Request.Builder().url(url)

        headers.forEach { builder.header(it.key, it.value) }
        builder.header("Cookie", getCookie(url))

        return builder.build()
    }

    private fun issueRequest(request: Request): WebResourceResponse? = try {
        val url = request.url.toString()
        val call = TurbolinksHttpClient.instance.newCall(request)
        val response = call.execute()

        if (response.isSuccessful) {
            setCookies(url, response)
            resourceResponse(response)
        } else {
            null
        }
    } catch (e: IOException) {
        // TODO offline cache
        null
    } catch (e: Exception) {
        null
    }

    private fun getCookie(url: String): String {
        return cookieManager.getCookie(url)
    }

    private fun setCookies(url: String, response: Response) {
        response.headers("Set-Cookie").forEach {
            cookieManager.setCookie(url, it)
        }
    }

    private fun resourceResponse(response: Response): WebResourceResponse {
        return WebResourceResponse(
            contentType(response),          // mimeType
            encoding(),                     // encoding
            response.code,                  // statusCode
            response.message,               // reasonPhrase
            headers(response),              // responseHeaders
            response.body?.byteStream()     // data
        )
    }

    private fun contentType(response: Response): String {
        return when (val contentType = response.headers["Content-Type"]) {
            null -> "text/plain"
            else -> contentType.removeSuffix("; charset=utf-8")
        }
    }

    private fun encoding(): String {
        return "utf-8"
    }

    private fun headers(response: Response): Map<String, String> {
        return response.headers.toMap()
    }
}
