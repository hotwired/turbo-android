package dev.hotwire.turbo.errors

import android.webkit.WebResourceResponse

/**
 * Errors representing HTTP status codes received from the server.
 */
sealed interface HttpError : TurboVisitError {
    val statusCode: Int
    val reasonPhrase: String?

    /**
     * Errors representing HTTP client errors in the 400..499 range.
     */
    sealed interface ClientError : HttpError {
        data object BadRequest : ClientError {
            override val statusCode = 400
            override val reasonPhrase = "Bad Request"
        }

        data object Unauthorized : ClientError {
            override val statusCode = 401
            override val reasonPhrase = "Unauthorized"
        }

        data object Forbidden : ClientError {
            override val statusCode = 403
            override val reasonPhrase = "Forbidden"
        }

        data object NotFound : ClientError {
            override val statusCode = 404
            override val reasonPhrase = "Not Found"
        }

        data object MethodNotAllowed : ClientError {
            override val statusCode = 405
            override val reasonPhrase = "Method Not Allowed"
        }

        data object NotAccessible : ClientError {
            override val statusCode = 406
            override val reasonPhrase = "Not Accessible"
        }

        data object ProxyAuthenticationRequired : ClientError {
            override val statusCode = 407
            override val reasonPhrase = "Proxy Authentication Required"
        }

        data object RequestTimeout : ClientError {
            override val statusCode = 408
            override val reasonPhrase = "Request Timeout"
        }

        data object Conflict : ClientError {
            override val statusCode = 409
            override val reasonPhrase = "Conflict"
        }

        data object MisdirectedRequest : ClientError {
            override val statusCode = 421
            override val reasonPhrase = "Misdirected Request"
        }

        data object UnprocessableEntity : ClientError {
            override val statusCode = 422
            override val reasonPhrase = "Unprocessable Entity"
        }

        data object PreconditionRequired : ClientError {
            override val statusCode = 428
            override val reasonPhrase = "Precondition Required"
        }

        data object TooManyRequests : ClientError {
            override val statusCode = 429
            override val reasonPhrase = "Too Many Requests"
        }

        data class Other(
            override val statusCode: Int,
            override val reasonPhrase: String?
        ) : ClientError
    }

    /**
     * Errors representing HTTP server errors in the 500..599 range.
     */
    sealed interface ServerError : HttpError {
        data object InternalServerError : ServerError {
            override val statusCode = 500
            override val reasonPhrase = "Internal Server Error"
        }

        data object NotImplemented : ServerError {
            override val statusCode = 501
            override val reasonPhrase = "Not Implemented"
        }

        data object BadGateway : ServerError {
            override val statusCode = 502
            override val reasonPhrase = "Bad Gateway"
        }

        data object ServiceUnavailable : ServerError {
            override val statusCode = 503
            override val reasonPhrase = "Service Unavailable"
        }

        data object GatewayTimeout : ServerError {
            override val statusCode = 504
            override val reasonPhrase = "Gateway Timeout"
        }

        data object HttpVersionNotSupported : ServerError {
            override val statusCode = 505
            override val reasonPhrase = "Http Version Not Supported"
        }

        data class Other(
            override val statusCode: Int,
            override val reasonPhrase: String?
        ) : ServerError
    }

    companion object {
        fun from(errorResponse: WebResourceResponse): HttpError {
            return getError(errorResponse.statusCode, errorResponse.reasonPhrase)
        }

        fun from(statusCode: Int): HttpError {
            return getError(statusCode, null)
        }

        private fun getError(statusCode: Int, reasonPhrase: String?): HttpError {
            if (statusCode in 400..499) {
                return ClientError::class.sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .firstOrNull { it.statusCode == statusCode }
                    ?: ClientError.Other(statusCode, reasonPhrase)
            }

            if (statusCode in 500..599) {
                return ServerError::class.sealedSubclasses
                    .map { it.objectInstance }
                    .firstOrNull { it?.statusCode == statusCode }
                    ?: ServerError.Other(statusCode, reasonPhrase)
            }

            throw IllegalArgumentException("Invalid HTTP error status code: $statusCode")
        }
    }
}
