package com.basecamp.turbolinks.demo

import android.content.Context
import androidx.annotation.ColorRes

internal fun Context.contentFromAsset(filePath: String): String {
    return assets.open(filePath).use {
        String(it.readBytes())
    }
}
