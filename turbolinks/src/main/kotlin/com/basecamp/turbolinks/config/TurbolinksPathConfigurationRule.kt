package com.basecamp.turbolinks.config

import com.basecamp.turbolinks.BuildConfig
import com.basecamp.turbolinks.util.TurbolinksLog
import com.google.gson.annotations.SerializedName
import java.util.regex.PatternSyntaxException

data class TurbolinksPathConfigurationRule(
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
