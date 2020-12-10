package dev.hotwire.turbo.visit

import dev.hotwire.turbo.session.TurbolinksSessionCallback

internal data class TurbolinksVisit(
    val location: String,
    val destinationIdentifier: Int,
    val restoreWithCachedSnapshot: Boolean,
    val reload: Boolean,
    var callback: TurbolinksSessionCallback?,   // Available while current visit
    var identifier: String = "",                // Updated after visitStarted()
    var completedOffline: Boolean = false,      // Updated from shouldInterceptRequest()
    val options: TurbolinksVisitOptions
)
