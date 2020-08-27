package com.basecamp.turbolinks

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.net.URI
import java.net.URL

internal fun Context.color(@ColorRes id: Int): Int {
    return resources.getColor(id, null)
}

internal fun Context.runOnUiThread(func: () -> Unit) {
    when (mainLooper.isCurrentThread) {
        true -> func()
        else -> Handler(mainLooper).post { func() }
    }
}

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        String(it.readBytes())
    }
}

internal fun Context.coroutineScope(): CoroutineScope {
    return (this as? AppCompatActivity)?.lifecycleScope ?: GlobalScope
}

internal fun Context.inflate(resource: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(resource, parent, false)
}

internal fun Any.toJson(): String {
    return gson().toJson(this)
}

internal fun <T> String.toObject(typeToken: TypeToken<T>): T {
    return gson().fromJson<T>(this, typeToken.type)
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

private fun gson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(VisitAction::class.java, VisitActionAdapter())
        .create()
}
