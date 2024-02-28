package dev.hotwire.turbo.errors

import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.isFeatureSupported

/**
 * Errors representing WebViewClient.ERROR_* errors received
 * from the WebView when attempting to load a page.
 * https://developer.android.com/reference/android/webkit/WebViewClient
 */
sealed interface WebError : TurboVisitError {
    val errorCode: Int
    val description: String?

    data object Unknown : WebError {
        override val errorCode = WebViewClientCompat.ERROR_UNKNOWN
        override val description = "Unknown"
    }

    data object HostLookup : WebError {
        override val errorCode = WebViewClientCompat.ERROR_HOST_LOOKUP
        override val description = "Host Lookup"
    }

    data object UnsupportedAuthScheme : WebError {
        override val errorCode = WebViewClientCompat.ERROR_UNSUPPORTED_AUTH_SCHEME
        override val description = "Unsupported Auth Scheme"
    }

    data object Authentication : WebError {
        override val errorCode = WebViewClientCompat.ERROR_AUTHENTICATION
        override val description = "Authentication"
    }

    data object ProxyAuthentication : WebError {
        override val errorCode = WebViewClientCompat.ERROR_PROXY_AUTHENTICATION
        override val description = "Proxy Authentication"
    }

    data object Connect : WebError {
        override val errorCode = WebViewClientCompat.ERROR_CONNECT
        override val description = "Connect"
    }

    data object IO : WebError {
        override val errorCode = WebViewClientCompat.ERROR_IO
        override val description = "IO"
    }

    data object Timeout : WebError {
        override val errorCode = WebViewClientCompat.ERROR_TIMEOUT
        override val description = "Timeout"
    }

    data object RedirectLoop : WebError {
        override val errorCode = WebViewClientCompat.ERROR_REDIRECT_LOOP
        override val description = "Redirect Loop"
    }

    data object UnsupportedScheme : WebError {
        override val errorCode = WebViewClientCompat.ERROR_UNSUPPORTED_SCHEME
        override val description = "Unsupported Scheme"
    }

    data object FailedSslHandshake : WebError {
        override val errorCode = WebViewClientCompat.ERROR_FAILED_SSL_HANDSHAKE
        override val description = "Failed SSL Handshake"
    }

    data object BadUrl : WebError {
        override val errorCode = WebViewClientCompat.ERROR_BAD_URL
        override val description = "Bad URL"
    }

    data object File : WebError {
        override val errorCode = WebViewClientCompat.ERROR_FILE
        override val description = "File"
    }

    data object FileNotFound : WebError {
        override val errorCode = WebViewClientCompat.ERROR_FILE_NOT_FOUND
        override val description = "File Not Found"
    }

    data object TooManyRequests : WebError {
        override val errorCode = WebViewClientCompat.ERROR_TOO_MANY_REQUESTS
        override val description = "Too Many Requests"
    }

    data object UnsafeResource : WebError {
        override val errorCode = WebViewClientCompat.ERROR_UNSAFE_RESOURCE
        override val description = "Unsafe Resource"
    }

    data class Other(
        override val errorCode: Int,
        override val description: String?
    ) : WebError

    companion object {
        fun from(error: WebResourceErrorCompat): WebError {
            val errorCode = if (isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE)) {
                error.errorCode
            } else {
                0
            }

            val description = if (isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
                error.description.toString()
            } else {
                null
            }

            return getError(errorCode, description)
        }

        fun from(errorCode: Int): WebError {
            return getError(errorCode, null)
        }

        private fun getError(errorCode: Int, description: String?): WebError {
            return WebError::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
                .firstOrNull { it.errorCode == errorCode }
                ?: Other(errorCode, description)
        }
    }
}
