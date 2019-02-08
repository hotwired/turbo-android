package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes

@Suppress("DEPRECATION")
internal fun Context.color(@ColorRes id: Int) = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> resources.getColor(id, null)
    else -> resources.getColor(id)
}

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        String(it.readBytes())
    }
}
