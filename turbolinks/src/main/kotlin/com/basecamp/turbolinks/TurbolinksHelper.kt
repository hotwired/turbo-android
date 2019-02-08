package com.basecamp.turbolinks

import android.os.Build
import android.os.Handler
import com.google.gson.GsonBuilder

internal fun isAtLeastMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

internal fun delay(milliseconds: Int, func: () -> Unit) {
    Handler().postDelayed({ func() }, milliseconds.toLong())
}

fun commaDelimitedJson(vararg params: Any): String? {
    val gson = GsonBuilder().disableHtmlEscaping().create()
    return params.joinToString(",") { gson.toJson(it) }
}
