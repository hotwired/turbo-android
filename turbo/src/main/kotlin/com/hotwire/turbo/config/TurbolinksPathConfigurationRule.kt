package com.hotwire.turbo.config

import com.hotwire.turbo.BuildConfig
import com.hotwire.turbo.util.TurbolinksLog
import com.google.gson.annotations.SerializedName
import java.util.regex.PatternSyntaxException

internal data class TurbolinksPathConfigurationRule(
    @SerializedName("patterns") val patterns: List<String>,
    @SerializedName("properties") val properties: TurbolinksPathConfigurationProperties
) {

    fun matches(path: String): Boolean {
        return patterns.any { numberOfMatches(path, it) > 0 }
    }

    private fun numberOfMatches(path: String, patternRegex: String): Int = try {
        Regex(patternRegex, RegexOption.IGNORE_CASE).find(path)?.groups?.size ?: 0
    } catch (e: PatternSyntaxException) {
        TurbolinksLog.e("PathConfiguration pattern error: ${e.description}")
        if (BuildConfig.DEBUG) throw e else 0
    }
}
