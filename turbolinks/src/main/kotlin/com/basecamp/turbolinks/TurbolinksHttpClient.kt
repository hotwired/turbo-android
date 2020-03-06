package com.basecamp.turbolinks

import android.content.Context
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object TurbolinksHttpClient {
    private var cache: Cache? = null
    private var httpCacheSize = 100L * 1024L * 1024L // 100 MBs

    internal val instance by lazy { buildNewHttpClient() }

    @Suppress("unused")
    fun setCacheSize(maxSize: Long) {
        this.httpCacheSize = maxSize
    }

    @Suppress("unused")
    fun invalidateCache() {
        try {
            cache?.evictAll()
        } catch (e: IOException) {
            TurbolinksLog.e(e.toString())
        }
    }

    internal fun enableCachingWith(context: Context) {
        if (cache == null) {
            val dir = File(context.cacheDir, "turbolinks_cache")
            cache = Cache(dir, httpCacheSize)
        }
    }

    private fun buildNewHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .addNetworkInterceptor(cacheControlNetworkInterceptor)

        cache?.let {
            builder.cache(it)
        }

        if (TurbolinksLog.enableDebugLogging) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val cacheControlNetworkInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = validateRequestCacheControl(chain.request())
            return validateResponseCacheControl(chain.proceed(request))
        }
    }

    private fun validateRequestCacheControl(request: Request): Request {
        if (cache == null || !shouldRewriteCacheControl(request)) {
            return request
        }

        return request.newBuilder()
            .removeHeader("Cache-Control")
            .build()
    }

    private fun validateResponseCacheControl(response: Response): Response {
        if (cache == null || !shouldRewriteCacheControl(response)) {
            return response
        }

        // Allow caching, but check with the origin server
        // for validation before using the cached copy. Prefer
        // a stale response over no response at all.
        val maxAge = maxOf(response.cacheControl.maxAgeSeconds, 0)
        val cacheControl = CacheControl.Builder()
            .maxAge(maxAge, TimeUnit.SECONDS)
            .maxStale(365, TimeUnit.DAYS)
            .build()

        return response.newBuilder()
            .removeHeader("Pragma")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }

    private fun shouldRewriteCacheControl(request: Request): Boolean {
        return  request.cacheControl.noStore ||
                request.cacheControl.noCache
    }

    private fun shouldRewriteCacheControl(response: Response): Boolean {
        return  response.header("Pragma") != null ||
                response.cacheControl.noStore ||
                response.cacheControl.noCache ||
                response.cacheControl.maxAgeSeconds <= 0 ||
                response.cacheControl.maxStaleSeconds <= 0
    }
}
