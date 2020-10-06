package com.basecamp.turbolinks.visit

import com.basecamp.turbolinks.util.TurbolinksSessionCallback

data class TurbolinksVisit(
    val location: String,
    val destinationIdentifier: Int,
    val restoreWithCachedSnapshot: Boolean,
    val reload: Boolean,
    var callback: TurbolinksSessionCallback?,   // Available while current visit
    var identifier: String = "",                // Updated after visitStarted()
    var completedOffline: Boolean = false,      // Updated from shouldInterceptRequest()
    val options: TurbolinksVisitOptions
)
