package dev.hotwire.turbo.session

import android.os.Bundle
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * Wraps all the relevant data returned by the modal after it closes. This allows the fragment
 * underneath the dismissed modal to process the result as necessary.
 *
 * @property location Location that the modal visited.
 * @property options Visit options that the modal used.
 * @property bundle Any additional data used by the navigation library.
 * @property shouldNavigate Whether the location should be navigated to based on its presentation property.
 */
data class TurboSessionModalResult(
    val location: String,
    val options: TurboVisitOptions,
    val bundle: Bundle?,
    val shouldNavigate: Boolean
)
