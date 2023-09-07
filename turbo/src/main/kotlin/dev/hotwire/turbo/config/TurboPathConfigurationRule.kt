package dev.hotwire.turbo.config

import dev.hotwire.turbo.BuildConfig
import dev.hotwire.turbo.util.TurboLog
import com.google.gson.annotations.SerializedName
import dev.hotwire.turbo.util.logError
import java.util.regex.PatternSyntaxException

internal data class TurboPathConfigurationRule(
    @SerializedName("patterns") val patterns: List<String>,
    @SerializedName("properties") val properties: TurboPathConfigurationProperties
) {

    fun matches(path: String): Boolean {
        return patterns.any { numberOfMatches(path, it) > 0 }
    }

    private fun numberOfMatches(path: String, patternRegex: String): Int = try {
        Regex(patternRegex, RegexOption.IGNORE_CASE).find(path)?.groups?.size ?: 0
    } catch (e: PatternSyntaxException) {
        logError("pathConfigurationPatternError", e)
        if (BuildConfig.DEBUG) throw e else 0
    }
}
