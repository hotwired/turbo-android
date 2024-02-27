package dev.hotwire.turbo.errors

sealed interface LoadError : TurboVisitError {
    data object NotPresent : LoadError
    data object NotReady : LoadError
}
