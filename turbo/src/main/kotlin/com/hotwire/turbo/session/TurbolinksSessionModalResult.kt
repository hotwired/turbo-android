package com.hotwire.turbo.session

import android.os.Bundle
import com.hotwire.turbo.visit.TurbolinksVisitOptions

/**
 * Wraps all the relevant data returned by the modal after it closes. This allows the fragment
 * underneath the dismissed modal to process the result as necessary.
 *
 * @property location Location that the modal visited.
 * @property options Visit options that the modal used.
 * @property bundle Any additional data used by the navigation library.
 * @property shouldNavigate Whether the location should be navigated to based on its presentation property.
 * @constructor Create empty Turbolinks session modal result
 */
data class TurbolinksSessionModalResult(
    val location: String,
    val options: TurbolinksVisitOptions,
    val bundle: Bundle?,
    val shouldNavigate: Boolean
)
