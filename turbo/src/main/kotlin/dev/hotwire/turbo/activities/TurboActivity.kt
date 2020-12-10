package dev.hotwire.turbo.activities

import dev.hotwire.turbo.delegates.TurboActivityDelegate

/**
 * Interface that should be implemented by any activity using Turbo. Ensures that the
 * activity provides a [TurboActivityDelegate] so the framework can communicate back and forth.
 *
 * @constructor Create empty Turbo activity
 */
interface TurboActivity {
    var delegate: TurboActivityDelegate
}
