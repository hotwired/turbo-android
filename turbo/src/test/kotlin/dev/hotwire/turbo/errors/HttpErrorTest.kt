package dev.hotwire.turbo.errors

import android.os.Build
import dev.hotwire.turbo.BaseUnitTest
import dev.hotwire.turbo.errors.HttpError.ClientError
import dev.hotwire.turbo.errors.HttpError.ServerError
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class HttpErrorTest : BaseUnitTest() {
    @Test
    fun clientErrors() {
        val errors = listOf(
            400 to ClientError.BadRequest,
            401 to ClientError.Unauthorized,
            403 to ClientError.Forbidden,
            404 to ClientError.NotFound,
            405 to ClientError.MethodNotAllowed,
            406 to ClientError.NotAccessible,
            407 to ClientError.ProxyAuthenticationRequired,
            408 to ClientError.RequestTimeout,
            409 to ClientError.Conflict,
            421 to ClientError.MisdirectedRequest,
            422 to ClientError.UnprocessableEntity,
            428 to ClientError.PreconditionRequired,
            429 to ClientError.TooManyRequests,
            430 to ClientError.Other(430, null),
            499 to ClientError.Other(499, null)
        )

        errors.forEach {
            val error = HttpError.from(it.first)
            assertThat(error).isEqualTo(it.second)
            assertThat(error.statusCode).isEqualTo(it.first)
        }
    }

    @Test
    fun serverErrors() {
        val errors = listOf(
            500 to ServerError.InternalServerError,
            501 to ServerError.NotImplemented,
            502 to ServerError.BadGateway,
            503 to ServerError.ServiceUnavailable,
            504 to ServerError.GatewayTimeout,
            505 to ServerError.HttpVersionNotSupported,
            506 to ServerError.Other(506, null),
            599 to ServerError.Other(599, null)
        )

        errors.forEach {
            val error = HttpError.from(it.first)
            assertThat(error).isEqualTo(it.second)
            assertThat(error.statusCode).isEqualTo(it.first)
        }
    }

    @Test
    fun unknownErrors() {
        val errors = listOf(
            399 to HttpError.UnknownError(399, null),
            600 to HttpError.UnknownError(600, null)
        )

        errors.forEach {
            val error = HttpError.from(it.first)
            assertThat(error).isEqualTo(it.second)
            assertThat(error.statusCode).isEqualTo(it.first)
        }
    }
}
