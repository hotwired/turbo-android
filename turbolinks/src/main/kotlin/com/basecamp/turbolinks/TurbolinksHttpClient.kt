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
        val dir = File(context.cacheDir, "turbolinks_cache")
        cache = Cache(dir, httpCacheSize)
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
        if (cache == null || request.cacheControl.maxAgeSeconds >= 0) {
            return request
        }

        // Prefer a stale response over no response
        return request.newBuilder()
            .cacheControl(CacheControl.Builder()
                .maxStale(365, TimeUnit.DAYS)
                .build())
            .build()
    }

    private fun validateResponseCacheControl(response: Response): Response {
        if (cache == null || !response.cacheControl.noStore) {
            return response
        }

        // Allow caching, but check with the origin server
        // for validation before using the cached copy
        return response.newBuilder()
            .header("Cache-Control", "max-age=0")
            .build()
    }
}
