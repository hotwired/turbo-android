package dev.hotwire.turbo.http

import android.webkit.WebResourceResponse

/**
 * Experimental: API may change, not ready for production use.
 */
interface TurboOfflineRequestHandler {
    fun getCacheStrategy(url: String): TurboOfflineCacheStrategy
    fun getCachedResponseHeaders(url: String): Map<String, String>?
    fun getCachedResponse(url: String, allowStaleResponse: Boolean = false): WebResourceResponse?
    fun getCachedSnapshot(url: String): WebResourceResponse?
    fun cacheResponse(url: String, response: WebResourceResponse): WebResourceResponse?
}
