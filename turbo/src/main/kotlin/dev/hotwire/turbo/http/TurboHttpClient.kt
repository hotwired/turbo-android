package dev.hotwire.turbo.http

import android.content.Context
import dev.hotwire.turbo.util.TurboLog
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Experimental: API may change, not ready for production use.
 */
object TurboHttpClient {
    private var cache: Cache? = null
    private var httpCacheSize = 100L * 1024L * 1024L // 100 MBs

    internal var instance = buildNewHttpClient()

    @Suppress("unused")
    fun setCacheSize(maxSize: Long) {
        httpCacheSize = maxSize
    }

    @Suppress("unused")
    fun invalidateCache() {
        try {
            cache?.evictAll()
        } catch (e: IOException) {
            TurboLog.e(e.toString())
        }
    }

    internal fun reset() {
        instance = buildNewHttpClient()
    }

    internal fun enableCachingWith(context: Context) {
        if (cache == null) {
            cache = Cache(
                directory = File(context.cacheDir, "turbo_cache"),
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

        if (TurboLog.enableDebugLogging) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
}
