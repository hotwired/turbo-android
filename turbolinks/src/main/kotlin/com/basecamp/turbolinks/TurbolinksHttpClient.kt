package com.basecamp.turbolinks

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object TurbolinksHttpClient {
    internal val instance by lazy { buildNewHttpClient() }
    private var headers: HashMap<String, String>? = null

    @Suppress("unused")
    fun setHeaders(headers: HashMap<String, String>) {
        this.headers = headers
    }

    private fun buildNewHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .addInterceptor(HttpInterceptor())

        if (TurbolinksLog.enableDebugLogging) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        }

        return builder.build()
    }

    private class HttpInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()

            headers?.forEach {
                request.header(it.key, it.value)
            }

            return chain.proceed(request.build())
        }
    }
}
