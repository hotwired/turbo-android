package com.basecamp.turbolinks

import android.content.Context
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object TurbolinksHttpClient {
    private var cache: Cache? = null
    private var httpCacheSize = 50L * 1024L * 1024L // 50 MBs

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
            val request = chain.request()
            return validateResponseCacheControl(chain.proceed(request))
        }
    }

    private fun validateResponseCacheControl(response: Response): Response {
        if (cache == null || !shouldRewriteCacheControl(response.cacheControl)) {
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
            .removeHeader("pragma")
            .header("cache-control", cacheControl.toString())
            .build()
    }

    private fun shouldRewriteCacheControl(cacheControl: CacheControl): Boolean {
        return  cacheControl.noStore ||
                cacheControl.noCache ||
                cacheControl.maxAgeSeconds <= 0 ||
                cacheControl.maxStaleSeconds <= 0
    }
}
