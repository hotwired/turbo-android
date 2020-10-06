package com.basecamp.turbolinks.nav

import android.os.Bundle
import androidx.navigation.fragment.FragmentNavigator
import com.basecamp.turbolinks.config.PathProperties
import com.basecamp.turbolinks.core.VisitOptions

data class TurbolinksNavOptions(
    val visitOptions: VisitOptions,
    val pathProperties: PathProperties? = null,
    val args: Bundle? = null,
    val navExtras: FragmentNavigator.Extras? = null
)
