package com.basecamp.turbolinks

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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
            .addNetworkInterceptor(cacheControlNetworkInterceptor)

        context?.let {
            builder.cache(cache(it))
        }

        if (TurbolinksLog.enableDebugLogging) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    private val cacheControlNetworkInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            val cacheControl = response.header("Cache-Control")

            return when (cacheControl?.contains("no-store")) {
                null, true -> {
                    // Caches must check with the origin server
                    // for validation before using the cached copy
                    response.newBuilder()
                        .header("Cache-Control", "no-cache")
                        .build()
                }
                else -> {
                    response
                }
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private fun cache(context: Context): Cache {
        val dir = File(context.cacheDir, "turbolinks_cache")
        return Cache(dir, httpCacheSize)
    }
}
