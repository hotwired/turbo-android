package dev.hotwire.turbo.http

import android.net.Uri
import android.webkit.WebResourceRequest

/**
 * Experimental: API may change, not ready for production use.
 */
internal class TurboPreCacheRequest(val url: String, val userAgent: String) : WebResourceRequest {

    override fun getUrl(): Uri {
        return Uri.parse(url)
    }

    override fun isRedirect(): Boolean {
        return false
    }

    override fun getMethod(): String {
        return "GET"
    }

    override fun getRequestHeaders(): Map<String, String> {
        return mapOf(
            "User-Agent" to userAgent
        )
    }

    override fun hasGesture(): Boolean {
        return false
    }

    override fun isForMainFrame(): Boolean {
        return true
    }
}
