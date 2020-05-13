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

        cache?.let {
            builder.addNetworkInterceptor(cacheControlNetworkInterceptor)
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
            val request = getCacheableRequest(chain.request())
            return getCacheableResponse(chain.proceed(request))
        }
    }

    private fun getCacheableRequest(request: Request): Request {
        return if (isNotCacheable(request)) {
            request.newBuilder()
                .removeHeader("Cache-Control")
                .build()
        } else {
            request
        }
    }

    private fun getCacheableResponse(response: Response): Response {
        return if (isNotCacheable(response)) {
            // Allow caching, but check with the origin server
            // for validation before using the cached copy. Prefer
            // a stale response over no response at all.
            val maxAge = maxOf(response.cacheControl.maxAgeSeconds, 0)
            val cacheControl = CacheControl.Builder()
                .maxAge(maxAge, TimeUnit.SECONDS)
                .maxStale(7, TimeUnit.DAYS)
                .build()

            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", cacheControl.toString())
                .build()
        } else {
            response
        }
    }

    private fun isNotCacheable(request: Request): Boolean {
        return  request.cacheControl.noStore ||
                request.cacheControl.noCache
    }

    private fun isNotCacheable(response: Response): Boolean {
        return  response.header("Pragma") != null ||
                response.cacheControl.noStore ||
                response.cacheControl.noCache ||
                response.cacheControl.maxAgeSeconds <= 0 ||
                response.cacheControl.maxStaleSeconds <= 0
    }
}
