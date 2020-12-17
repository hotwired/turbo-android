package dev.hotwire.turbo.activities

import dev.hotwire.turbo.delegates.TurboActivityDelegate
import dev.hotwire.turbo.session.TurboSessionNavHostFragment

/**
 * Interface that should be implemented by any Activity using Turbo. Ensures that the
 * Activity provides a [TurboActivityDelegate] so the framework can initialize the
 * [TurboSessionNavHostFragment] hosted in your Activity's layout resource.
 */
interface TurboActivity {
    var delegate: TurboActivityDelegate
}
