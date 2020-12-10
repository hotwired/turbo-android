package dev.hotwire.turbo.activities

import dev.hotwire.turbo.delegates.TurbolinksActivityDelegate

/**
 * Interface that should be implemented by any activity using Turbolinks. Ensures that the
 * activity provides a [TurbolinksActivityDelegate] so the framework can communicate back and forth.
 *
 * @constructor Create empty Turbolinks activity
 */
interface TurbolinksActivity {
    var delegate: TurbolinksActivityDelegate
}
