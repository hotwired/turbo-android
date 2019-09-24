package com.basecamp.turbolinks

import android.os.Handler
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import com.google.gson.GsonBuilder

internal fun delay(milliseconds: Int, func: () -> Unit) {
    Handler().postDelayed({ func() }, milliseconds.toLong())
}

fun commaDelimitedJson(vararg params: Any): String? {
    val gson = GsonBuilder().disableHtmlEscaping().create()
    return params.joinToString(",") { gson.toJson(it) }
}

val WebView.packageName: String?
    get() = WebViewCompat.getCurrentWebViewPackage(context)?.packageName

val WebView.versionName: String?
    get() = WebViewCompat.getCurrentWebViewPackage(context)?.versionName

val WebView.majorVersion: Int?
    get() = versionName?.substringBefore(".")?.toIntOrNull()
