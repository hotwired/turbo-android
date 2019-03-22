package com.basecamp.turbolinks

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.regex.PatternSyntaxException
import kotlin.text.RegexOption.IGNORE_CASE

class PathConfiguration(
    @SerializedName("url") var url: String? = null,
    @SerializedName("rules") var rules: List<PathRule> = emptyList()) {

    internal var repository = Repository()

    init {
        downloadRemoteConfiguration()
    }

    fun properties(path: String): PathProperties {
        val properties = PathProperties()

        for (rule in rules) when (rule.matches(path)) {
            true -> properties.putAll(rule.properties)
        }

        return properties
    }

    fun reloadRemoteConfiguration() {
        downloadRemoteConfiguration()
    }

    private fun downloadRemoteConfiguration() {
        val configurationUrl = url ?: return

        GlobalScope.launch {
            repository.getRemotePathConfiguration(configurationUrl)?.let {
                it.rules = rules
            }
        }
    }

    companion object {
        fun load(json: String): PathConfiguration {
            return json.toObject(object : TypeToken<PathConfiguration>() {})
        }
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
