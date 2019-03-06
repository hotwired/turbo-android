package com.basecamp.turbolinks

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.annotation.ColorRes
import java.net.URI
import java.net.URL

internal fun WebView.runJavascript(javascript: String) {
    context.runOnUiThread {
        evaluateJavascript(javascript) {}
    }
}

internal fun Context.color(@ColorRes id: Int): Int {
    return resources.getColor(id, null)
}

internal fun Context.runOnUiThread(func: () -> Unit) {
    Handler(mainLooper).post { func() }
}

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        String(it.readBytes())
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
