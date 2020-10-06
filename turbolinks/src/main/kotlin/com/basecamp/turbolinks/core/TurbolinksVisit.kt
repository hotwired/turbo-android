package com.basecamp.turbolinks.core

import com.basecamp.turbolinks.util.TurbolinksCallback

data class TurbolinksVisit(
    val location: String,
    val destinationIdentifier: Int,
    val restoreWithCachedSnapshot: Boolean,
    val reload: Boolean,
    var callback: TurbolinksCallback?,   // Available while current visit
    var identifier: String = "",                // Updated after visitStarted()
    var completedOffline: Boolean = false,      // Updated from shouldInterceptRequest()
    val options: VisitOptions
)
