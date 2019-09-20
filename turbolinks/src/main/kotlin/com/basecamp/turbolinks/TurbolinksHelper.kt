package com.basecamp.turbolinks

import android.os.Handler
import android.webkit.WebView
import com.google.gson.GsonBuilder

internal fun delay(milliseconds: Int, func: () -> Unit) {
    Handler().postDelayed({ func() }, milliseconds.toLong())
}

fun commaDelimitedJson(vararg params: Any): String? {
    val gson = GsonBuilder().disableHtmlEscaping().create()
    return params.joinToString(",") { gson.toJson(it) }
}

val WebView.chromeVersion: String?
    get() {
        val regex = "Chrome/([\\d.]+) Mobile".toRegex()
        val result = regex.find(settings.userAgentString)
        return result?.groupValues?.get(1)
    }

val WebView.chromeMajorVersion: Int?
    get() = chromeVersion?.substringBefore(".")?.toIntOrNull()
