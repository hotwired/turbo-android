package dev.hotwire.turbo.demo.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import dev.hotwire.turbo.config.TurboPathConfigurationProperties

fun Context.drawable(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

val TurboPathConfigurationProperties.title: String?
    get() = get("title")
