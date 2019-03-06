package com.basecamp.turbolinks.demosimple

import android.content.Context
import androidx.annotation.ColorRes

internal fun Context.color(@ColorRes id: Int): Int {
    return resources.getColor(id, null)
}
