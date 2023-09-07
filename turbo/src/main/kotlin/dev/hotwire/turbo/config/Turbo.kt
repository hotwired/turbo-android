package dev.hotwire.turbo.config

object Turbo {
    val config: TurboConfig = TurboConfig()

    /**
     * Provides a standard substring to be included in your WebView's user agent
     * to identify itself as a Turbo Native app.
     */
    fun userAgentSubstring(): String {
        return "Turbo Native Android"
    }
}
