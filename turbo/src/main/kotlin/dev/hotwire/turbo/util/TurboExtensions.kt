package dev.hotwire.turbo.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.webkit.WebResourceRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.hotwire.turbo.visit.TurboVisitAction
import dev.hotwire.turbo.visit.TurboVisitActionAdapter
import java.io.File

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

internal fun String.extract(patternRegex: String): String? {
    val regex = Regex(patternRegex, RegexOption.IGNORE_CASE)
    return regex.find(this)?.groups?.get(1)?.value
}

internal fun File.deleteAllFilesInDirectory() {
    if (!isDirectory) return

    listFiles()?.forEach {
        it.delete()
    }
}

internal fun WebResourceRequest.isHttpGetRequest(): Boolean {
    return method.equals("GET", ignoreCase = true) &&
        url.scheme?.startsWith("HTTP", ignoreCase = true) == true
}

internal fun Any.toJson(): String {
    return gson.toJson(this)
}

internal fun <T> String.toObject(typeToken: TypeToken<T>): T {
    return gson.fromJson(this, typeToken.type)
}

internal fun Int.animateColorTo(toColor: Int, duration: Long = 150, onUpdate: (Int) -> Unit) {
    ValueAnimator.ofObject(ArgbEvaluator(), this, toColor).apply {
        this.duration = duration
        this.addUpdateListener {
            val color = it.animatedValue as Int?
            color?.let { onUpdate(color) }
        }
    }.start()
}

private val gson: Gson = GsonBuilder()
    .registerTypeAdapter(TurboVisitAction::class.java, TurboVisitActionAdapter())
    .create()
