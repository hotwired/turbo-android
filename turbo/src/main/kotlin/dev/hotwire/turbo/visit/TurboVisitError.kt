package dev.hotwire.turbo.visit

data class TurboVisitError(
    /**
     * The [TurboVisitErrorType] type of error received.
     */
    val type: TurboVisitErrorType,

    /**
     * The error code associated with the [TurboVisitErrorType] type.
     */
    val code: Int,

    /**
     * The (optional) description of the error.
     */
    val description: String? = null
)
