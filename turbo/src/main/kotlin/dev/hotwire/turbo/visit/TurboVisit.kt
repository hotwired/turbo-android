package dev.hotwire.turbo.visit

import dev.hotwire.turbo.session.TurboSessionCallback

internal data class TurboVisit(
    val location: String,
    val destinationIdentifier: Int,
    val restoreWithCachedSnapshot: Boolean,
    val reload: Boolean,
    var callback: TurboSessionCallback?,        // Available while current visit
    var identifier: String = "",                // Updated after visitStarted()
    var completedOffline: Boolean = false,      // Updated from shouldInterceptRequest()
    val options: TurboVisitOptions
)
