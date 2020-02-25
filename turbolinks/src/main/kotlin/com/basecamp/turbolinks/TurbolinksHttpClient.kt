package com.basecamp.turbolinks

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

object TurbolinksHttpClient {
    private var context: Context? = null
    private var httpCacheSize = 50L * 1024L * 1024L // 50 MBs

    internal val instance by lazy { buildNewHttpClient() }

    @Suppress("unused")
    fun setCacheSize(maxSize: Long) {
        this.httpCacheSize = maxSize
    }

    internal fun enableCachingWith(context: Context) {
        this.context = context.applicationContext
    }

    private fun buildNewHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)

        context?.let {
            builder.cache(cache(it))
        }

        if (TurbolinksLog.enableDebugLogging) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        }

        return builder.build()
    }

    private fun cache(context: Context): Cache {
        val dir = File(context.cacheDir, "turbolinks_cache")
        return Cache(dir, httpCacheSize)
    }
}
