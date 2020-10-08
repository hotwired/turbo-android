package com.basecamp.turbolinks.demo.extensions

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

val BottomNavigationView.isAlreadyVisible
    get() = isVisible && bottom == (parent as ConstraintLayout).bottom

fun BottomNavigationView.animateVisibility(visible: Boolean) {
    val constraintLayout = parent as ConstraintLayout

    if (visible) {
        val transition = ChangeBounds().apply { duration = 150 }
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
    }

    val constraintSet = ConstraintSet().apply {
        clone(constraintLayout)
        clear(id, ConstraintSet.BOTTOM)
        clear(id, ConstraintSet.TOP)
    }

    val anchor = if (visible) ConstraintSet.BOTTOM else ConstraintSet.TOP
    constraintSet.connect(id, anchor, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
    constraintSet.applyTo(constraintLayout)

    // Reset the view visibility if it is currently
    // gone, such as from the virtual keyboard.
    if (!isVisible && visible) {
        isVisible = visible
    }
}
