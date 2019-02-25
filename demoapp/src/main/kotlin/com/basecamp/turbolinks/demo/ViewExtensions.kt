package com.basecamp.turbolinks.demo

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

fun View.translationYAnimator(startY: Float, endY: Float, duration: Long, startDelay: Long = 0): ObjectAnimator {
    val property = PropertyValuesHolder.ofFloat("translationY", startY, endY)

    return ObjectAnimator.ofPropertyValuesHolder(this, property).apply {
        this.duration = duration
        this.startDelay = startDelay
    }
}
