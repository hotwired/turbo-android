package dev.hotwire.turbo.config

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import dev.hotwire.turbo.nav.TurboNavPresentation
import dev.hotwire.turbo.nav.TurboNavPresentationContext
import com.google.gson.annotations.SerializedName
import java.net.URL

class TurboPathConfiguration(context: Context) {
    private val cachedProperties: HashMap<String, TurboPathConfigurationProperties> = hashMapOf()

    @SerializedName("rules")
    internal var rules: List<TurboPathConfigurationRule> = emptyList()

    @SerializedName("settings")
    var settings: TurboPathConfigurationSettings = TurboPathConfigurationSettings()

    internal var loader = TurboPathConfigurationLoader(context.applicationContext)

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

    fun properties(location: String): TurboPathConfigurationProperties {
        cachedProperties[location]?.let { return it }

        val properties = TurboPathConfigurationProperties()
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

typealias TurboPathConfigurationProperties = HashMap<String, String>
typealias TurboPathConfigurationSettings = HashMap<String, String>

val TurboPathConfigurationProperties.presentation: TurboNavPresentation
    @SuppressLint("DefaultLocale") get() = try {
        val value = get("presentation") ?: "default"
        TurboNavPresentation.valueOf(value.toUpperCase())
    } catch (e: IllegalArgumentException) {
        TurboNavPresentation.DEFAULT
    }

val TurboPathConfigurationProperties.context: TurboNavPresentationContext
    @SuppressLint("DefaultLocale") get() = try {
        val value = get("context") ?: "default"
        TurboNavPresentationContext.valueOf(value.toUpperCase())
    } catch (e: IllegalArgumentException) {
        TurboNavPresentationContext.DEFAULT
    }

val TurboPathConfigurationProperties.uri: Uri
    get() = Uri.parse(get("uri"))

val TurboPathConfigurationProperties.fallbackUri: Uri?
    get() = get("fallback_uri")?.let { Uri.parse(it) }

val TurboPathConfigurationProperties.title: String?
    get() = get("title")

val TurboPathConfigurationProperties.pullToRefreshEnabled: Boolean
    get() = get("pull_to_refresh_enabled")?.toBoolean() ?: false

val TurboPathConfigurationSettings.screenshotsEnabled: Boolean
    get() = get("screenshots_enabled")?.toBoolean() ?: true