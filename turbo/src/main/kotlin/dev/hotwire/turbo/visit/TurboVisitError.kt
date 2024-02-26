package dev.hotwire.turbo.visit

data class TurboVisitError(
    /**
     *
     */
    val type: TurboVisitErrorType,

    /**
     *
     */
    val code: Int,

    /**
     * 
     */
    val description: String? = null
)
