package com.basecamp.turbolinks

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal object Http {
    var sharedHttpClient = buildNewHttpClient()

    private fun buildNewHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS)

        return builder.build()
    }
}
