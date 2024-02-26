package dev.hotwire.turbo.visit

enum class TurboVisitErrorType {
    /**
     * Represents an error when Turbo and the javascript adapter fails to
     * load on the page.
     */
    LOAD_ERROR,

    /**
     * Represents an error received from your server with an HTTP status code.
     * The code corresponds to the status code received from your server.
     */
    HTTP_ERROR,

    /**
     * Represents an [androidx.webkit.WebResourceErrorCompat] error received
     * from the WebView when attempting to load the page. The code corresponds
     * to one of the ERROR_* constants in [androidx.webkit.WebViewClientCompat].
     */
    WEB_RESOURCE_ERROR,

    /**
     * Represents an [android.net.http.SslError] error received from the WebView
     * when attempting to load the page. The code corresponds to one of the
     * SSL_* constants in [android.net.http.SslError].
     */
    WEB_SSL_ERROR
}
