package dev.hotwire.turbo.util

import android.content.Context
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.hotwire.turbo.visit.TurboVisitAction
import dev.hotwire.turbo.visit.TurboVisitActionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

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

internal fun Any.toJson(): String {
    return gson.toJson(this)
}

internal fun <T> String.toObject(typeToken: TypeToken<T>): T {
    return gson.fromJson(this, typeToken.type)
}

private val gson: Gson = GsonBuilder()
    .registerTypeAdapter(TurboVisitAction::class.java, TurboVisitActionAdapter())
    .create()
