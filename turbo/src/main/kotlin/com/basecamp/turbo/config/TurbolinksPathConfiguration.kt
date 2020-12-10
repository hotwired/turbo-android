package com.basecamp.turbo.config

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.basecamp.turbo.nav.TurbolinksNavPresentation
import com.basecamp.turbo.nav.TurbolinksNavPresentationContext
import com.google.gson.annotations.SerializedName
import java.net.URL

class TurbolinksPathConfiguration(context: Context) {
    private val cachedProperties: HashMap<String, TurbolinksPathConfigurationProperties> = hashMapOf()

    @SerializedName("rules")
    internal var rules: List<TurbolinksPathConfigurationRule> = emptyList()

    @SerializedName("settings")
    var settings: TurbolinksPathConfigurationSettings = TurbolinksPathConfigurationSettings()

    internal var loader = TurbolinksPathConfigurationLoader(context.applicationContext)

    data class Location(
        val assetFilePath: String? = null,
        val remoteFileUrl: String? = null
    )

    fun load(location: Location) {
        loader.load(location) {
            cachedProperties.clear()
            rules = it.rules
            settings = it.settings
        }
    }

    fun properties(location: String): TurbolinksPathConfigurationProperties {
        cachedProperties[location]?.let { return it }

        val properties = TurbolinksPathConfigurationProperties()
        val path = path(location)

        for (rule in rules) when (rule.matches(path)) {
            true -> properties.putAll(rule.properties)
        }

        cachedProperties[location] = properties

        return properties
    }

    private fun path(location: String): String {
        val url = URL(location)

        return when (url.query) {
            null -> url.path
            else -> "${url.path}?${url.query}"
        }
    }
}

typealias TurbolinksPathConfigurationProperties = HashMap<String, String>
typealias TurbolinksPathConfigurationSettings = HashMap<String, String>

val TurbolinksPathConfigurationProperties.presentation: TurbolinksNavPresentation
    @SuppressLint("DefaultLocale") get() = try {
        val value = get("presentation") ?: "default"
        TurbolinksNavPresentation.valueOf(value.toUpperCase())
    } catch (e: IllegalArgumentException) {
        TurbolinksNavPresentation.DEFAULT
    }

val TurbolinksPathConfigurationProperties.context: TurbolinksNavPresentationContext
    @SuppressLint("DefaultLocale") get() = try {
        val value = get("context") ?: "default"
        TurbolinksNavPresentationContext.valueOf(value.toUpperCase())
    } catch (e: IllegalArgumentException) {
        TurbolinksNavPresentationContext.DEFAULT
    }

val TurbolinksPathConfigurationProperties.uri: Uri
    get() = Uri.parse(get("uri"))

val TurbolinksPathConfigurationProperties.fallbackUri: Uri?
    get() = get("fallback_uri")?.let { Uri.parse(it) }

val TurbolinksPathConfigurationProperties.pullToRefreshEnabled: Boolean
    get() = get("pull_to_refresh_enabled")?.toBoolean() ?: false

val TurbolinksPathConfigurationSettings.screenshotsEnabled: Boolean
    get() = get("screenshots_enabled")?.toBoolean() ?: true