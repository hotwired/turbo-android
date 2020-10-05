package com.basecamp.turbolinks.nav

import android.os.Bundle
import androidx.navigation.fragment.FragmentNavigator
import com.basecamp.turbolinks.core.VisitOptions
import com.basecamp.turbolinks.config.PathProperties

data class TurbolinksNavOptions (
        val visitOptions: VisitOptions,
        val pathProperties: PathProperties? = null,
        val args: Bundle? = null,
        val navExtras: FragmentNavigator.Extras? = null
)
