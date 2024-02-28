package dev.hotwire.turbo.errors

/**
 * Errors representing when turbo.js or the native adapter fails
 * to load on a page.
 */
sealed interface LoadError : TurboVisitError {
    val description: String

    data object NotPresent : LoadError {
        override val description = "Turbo Not Present"
    }

    data object NotReady : LoadError {
        override val description = "Turbo Not Ready"
    }
}
