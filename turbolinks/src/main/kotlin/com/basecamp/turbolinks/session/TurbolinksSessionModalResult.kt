package com.basecamp.turbolinks.session

import android.os.Bundle
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

data class TurbolinksSessionModalResult(
    val location: String,
    val options: TurbolinksVisitOptions,
    val bundle: Bundle?,
    val shouldNavigate: Boolean
)
