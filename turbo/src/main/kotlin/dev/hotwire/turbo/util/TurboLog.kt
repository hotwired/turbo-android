package dev.hotwire.turbo.util

import android.util.Log
import dev.hotwire.turbo.config.Turbo

internal object TurboLog {
    private const val DEFAULT_TAG = "TurboLog"

    private val debugEnabled get() = Turbo.config.debugLoggingEnabled

    internal fun d(msg: String) = log(Log.DEBUG, msg)

    internal fun e(msg: String) = log(Log.ERROR, msg)

    private fun log(logLevel: Int, msg: String) {
        when (logLevel) {
            Log.DEBUG -> if (debugEnabled) Log.d(DEFAULT_TAG, msg)
            Log.ERROR -> Log.e(DEFAULT_TAG, msg)
        }
    }
}

internal fun logEvent(event: String, attributes: List<Pair<String, Any>>) {
    val description = attributes.joinToString(prefix = "[", postfix = "]", separator = ", ") {
        "${it.first}: ${it.second}"
    }
    TurboLog.d("$event ".padEnd(35, '.') + " $description")
}

internal fun logError(event: String, error: Exception) {
    TurboLog.e("$event: ${error.stackTraceToString()}")
}
