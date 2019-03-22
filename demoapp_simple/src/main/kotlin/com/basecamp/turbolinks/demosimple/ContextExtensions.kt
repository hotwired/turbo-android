package com.basecamp.turbolinks.demosimple

import android.content.Context
import androidx.annotation.ColorRes

internal fun Context.color(@ColorRes id: Int): Int {
    return resources.getColor(id, null)
}

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        String(it.readBytes())
    }
}
