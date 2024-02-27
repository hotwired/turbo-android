package dev.hotwire.turbo.errors

import android.net.http.SslError
import android.os.Build
import dev.hotwire.turbo.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class WebSslErrorTest : BaseUnitTest() {
    @Test
    fun sslErrors() {
        val errors = listOf(
            SslError.SSL_NOTYETVALID to WebSslError.NotYetValid,
            SslError.SSL_EXPIRED to WebSslError.Expired,
            SslError.SSL_IDMISMATCH to WebSslError.IdMismatch,
            SslError.SSL_UNTRUSTED to WebSslError.Untrusted,
            SslError.SSL_DATE_INVALID to WebSslError.DateInvalid,
            SslError.SSL_INVALID to WebSslError.Invalid,
            -1 to WebSslError.Other(-1),
            6 to WebSslError.Other(6),
        )

        errors.forEach {
            val error = WebSslError.from(it.first)
            assertThat(error).isEqualTo(it.second)
            assertThat(error.errorCode).isEqualTo(it.first)
        }
    }
}
