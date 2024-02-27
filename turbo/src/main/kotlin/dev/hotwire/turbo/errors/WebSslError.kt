package dev.hotwire.turbo.errors

import android.net.http.SslError

sealed interface WebSslError : TurboVisitError {
    val errorCode: Int
    val description: String?

    data object NotYetValid : WebSslError {
        override val errorCode = SslError.SSL_NOTYETVALID
        override val description = "Not Yet Valid"
    }

    data object Expired : WebSslError {
        override val errorCode = SslError.SSL_EXPIRED
        override val description = "Expired"
    }

    data object IdMismatch : WebSslError {
        override val errorCode = SslError.SSL_IDMISMATCH
        override val description = "ID Mismatch"
    }

    data object Untrusted : WebSslError {
        override val errorCode = SslError.SSL_UNTRUSTED
        override val description = "Untrusted"
    }

    data object DateInvalid : WebSslError {
        override val errorCode = SslError.SSL_DATE_INVALID
        override val description = "Date Invalid"
    }

    data object Invalid : WebSslError {
        override val errorCode = SslError.SSL_INVALID
        override val description = "Invalid"
    }

    data class Other(override val errorCode: Int) : WebSslError {
        override val description = null
    }

    companion object {
        fun from(error: SslError): WebSslError {
            return getError(error.primaryError)
        }

        fun from(errorCode: Int): WebSslError {
            return getError(errorCode)
        }

        private fun getError(errorCode: Int): WebSslError {
            return WebSslError::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
                .firstOrNull { it.errorCode == errorCode }
                ?: Other(errorCode)
        }
    }
}
