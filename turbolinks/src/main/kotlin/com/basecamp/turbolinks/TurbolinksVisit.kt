package com.basecamp.turbolinks

data class TurbolinksVisit(
        val location: String,
        val destinationIdentifier: Int,
        var restoreWithCachedSnapshot: Boolean,
        val reload: Boolean,
        val callback: TurbolinksSessionCallback
)
