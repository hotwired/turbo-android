package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import androidx.annotation.ColorRes
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import java.net.URI
import java.net.URL

internal fun WebView.executeJavascript(jsFunction: String) {
    context.runOnUiThread { loadUrl("javascript: $jsFunction") }
}

@SuppressLint("NewApi")
@Suppress("DEPRECATION")
internal fun Context.color(@ColorRes id: Int) = when {
    isAtLeastMarshmallow() -> resources.getColor(id, null)
    else -> resources.getColor(id)
}

internal fun Context.runOnUiThread(func: () -> Unit) {
    Handler(mainLooper).post { func() }
}

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        Base64.encodeToString(it.readBytes(), Base64.NO_WRAP)
    }
}

internal fun Context.inflate(resource: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(resource, parent, false)
}

internal fun String.urlEncode(): String {
    try {
        val url = URL(this)
        val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        return uri.toURL().toString()
    } catch (e: Exception) {
        return ""
    }
}

internal fun View.visible() {
    visibility = View.VISIBLE
}

internal fun View.visible(visible: Boolean) {
    if (visible) visible() else gone()
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.invisible(invisible: Boolean) {
    if (invisible) invisible() else visible()
}

internal fun View.gone() {
    visibility = View.GONE
}
