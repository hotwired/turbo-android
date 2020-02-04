package com.basecamp.turbolinks

import android.os.Bundle
import androidx.navigation.fragment.FragmentNavigator

data class TurbolinksNavOptions (
    val visitOptions: VisitOptions,
    val pathProperties: PathProperties? = null,
    val args: Bundle? = null,
    val navExtras: FragmentNavigator.Extras? = null
)
