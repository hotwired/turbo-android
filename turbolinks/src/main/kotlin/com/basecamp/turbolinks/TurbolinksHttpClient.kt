package com.basecamp.turbolinks

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object TurbolinksHttpClient {
    private var cache: Cache? = null
    private var httpCacheSize = 100L * 1024L * 1024L // 100 MBs

    internal var instance = buildNewHttpClient()

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

    internal fun reset() {
        instance = buildNewHttpClient()
    }

    internal fun enableCachingWith(context: Context) {
        if (cache == null) {
            cache = Cache(
                directory = File(context.cacheDir, "turbolinks_cache"),
                maxSize = httpCacheSize
            )
            reset()
        }
    }

    private fun buildNewHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)

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
}
