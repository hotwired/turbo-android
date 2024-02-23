package dev.hotwire.turbo.config

import dev.hotwire.turbo.http.TurboHttpClient

class TurboConfig internal constructor() {
    /**
     * Enables/disables debug logging. This should be disabled in production environments.
     * Disabled by default.
     *
     */
    var debugLoggingEnabled = false
        set(value) {
            field = value
            TurboHttpClient.reset()
        }
}
