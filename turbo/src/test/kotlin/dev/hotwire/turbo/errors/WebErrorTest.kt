package dev.hotwire.turbo.errors

import android.os.Build
import androidx.webkit.WebViewClientCompat
import dev.hotwire.turbo.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class WebErrorTest : BaseUnitTest() {
    @Test
    fun webErrors() {
        val errors = listOf(
            WebViewClientCompat.ERROR_UNKNOWN to WebError.Unknown,
            WebViewClientCompat.ERROR_HOST_LOOKUP to WebError.HostLookup,
            WebViewClientCompat.ERROR_UNSUPPORTED_AUTH_SCHEME to WebError.UnsupportedAuthScheme,
            WebViewClientCompat.ERROR_AUTHENTICATION to WebError.Authentication,
            WebViewClientCompat.ERROR_PROXY_AUTHENTICATION to WebError.ProxyAuthentication,
            WebViewClientCompat.ERROR_CONNECT to WebError.Connect,
            WebViewClientCompat.ERROR_IO to WebError.IO,
            WebViewClientCompat.ERROR_TIMEOUT to WebError.Timeout,
            WebViewClientCompat.ERROR_REDIRECT_LOOP to WebError.RedirectLoop,
            WebViewClientCompat.ERROR_UNSUPPORTED_SCHEME to WebError.UnsupportedScheme,
            WebViewClientCompat.ERROR_FAILED_SSL_HANDSHAKE to WebError.FailedSslHandshake,
            WebViewClientCompat.ERROR_BAD_URL to WebError.BadUrl,
            WebViewClientCompat.ERROR_FILE to WebError.File,
            WebViewClientCompat.ERROR_FILE_NOT_FOUND to WebError.FileNotFound,
            WebViewClientCompat.ERROR_TOO_MANY_REQUESTS to WebError.TooManyRequests,
            WebViewClientCompat.ERROR_UNSAFE_RESOURCE to WebError.UnsafeResource,
            -17 to WebError.Other(-17, null),
            1 to WebError.Other(1, null),
        )

        errors.forEach {
            val error = WebError.from(it.first)
            assertThat(error).isEqualTo(it.second)
            assertThat(error.errorCode).isEqualTo(it.first)
        }
    }
}
