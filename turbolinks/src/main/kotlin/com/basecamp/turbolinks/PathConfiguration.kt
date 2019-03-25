package com.basecamp.turbolinks

import android.content.Context
import com.google.gson.annotations.SerializedName
import java.util.regex.PatternSyntaxException
import kotlin.text.RegexOption.IGNORE_CASE

class PathConfiguration(context: Context) {
    @SerializedName("rules") var rules: List<PathRule> = emptyList()

    internal var loader = PathConfigurationLoader(context.applicationContext)

    data class Location(
            val assetFilePath: String? = null,
            val remoteFileUrl: String? = null
    )

    fun load(location: Location) {
        loader.load(location) {
            rules = it.rules
        }
    }

    fun properties(path: String): PathProperties {
        val properties = PathProperties()

        for (rule in rules) when (rule.matches(path)) {
            true -> properties.putAll(rule.properties)
        }

        return properties
    }
}

data class PathRule(
    @SerializedName("patterns") val patterns: List<String>,
    @SerializedName("properties") val properties: PathProperties) {

    fun matches(path: String): Boolean {
        return patterns.any { numberOfMatches(path, it) > 0 }
    }

    private fun numberOfMatches(path: String, patternRegex: String): Int = try {
        Regex(patternRegex, IGNORE_CASE).find(path)?.groups?.size ?: 0
    } catch (e: PatternSyntaxException) {
        TurbolinksLog.e("PathConfiguration pattern error: ${e.description}")
        if (BuildConfig.DEBUG) throw e else 0
    }
}

typealias PathProperties = HashMap<String, String>

val PathProperties.context: PresentationContext get() = try {
    val value = get("context") ?: "default"
    PresentationContext.valueOf(value.toUpperCase())
} catch (e: IllegalArgumentException) {
    PresentationContext.DEFAULT
}
