package dev.hotwire.turbo.errors

sealed interface LoadError : TurboVisitError {
    val description: String

    data object NotPresent : LoadError {
        override val description = "Turbo Not Present"
    }

    data object NotReady : LoadError {
        override val description = "Turbo Not Ready"
    }
}
