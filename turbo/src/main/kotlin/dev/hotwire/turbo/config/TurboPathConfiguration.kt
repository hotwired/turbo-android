package dev.hotwire.turbo.config

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import dev.hotwire.turbo.nav.TurboNavPresentation
import dev.hotwire.turbo.nav.TurboNavPresentationContext
import com.google.gson.annotations.SerializedName
import dev.hotwire.turbo.nav.TurboNavQueryStringPresentation
import java.net.URL

/**
 * Provides the ability to load, parse, and retrieve url path
 * properties from the app's JSON configuration file.
 */
class TurboPathConfiguration(context: Context) {
    private val cachedProperties: HashMap<String, TurboPathConfigurationProperties> = hashMapOf()

    internal val loader = TurboPathConfigurationLoader(context.applicationContext)

    @SerializedName("rules")
    internal var rules: List<TurboPathConfigurationRule> = emptyList()

    /**
     * Gets the top-level settings specified in the app's path configuration.
     * The settings are map of key/value `String` items.
     */
    @SerializedName("settings")
    var settings: TurboPathConfigurationSettings = TurboPathConfigurationSettings()
        private set

    /**
     * Represents the location of the app's path configuration JSON file(s).
     */
    data class Location(
        /**
         * The location of the locally bundled configuration file. Providing a
         * local configuration file is highly recommended, so your app's
         * configuration is available immediately at startup. This must be
         * located in the app's `assets` directory. For example, a configuration
         * located in `assets/json/configuration.json` would specify the path
         * without the `assets` prefix: `"json/configuration.json"`.
         */
        val assetFilePath: String? = null,

        /**
         * The location of the remote configuration file on your server. This
         * file must be publicly available via a GET request. The file will be
         * automatically downloaded and cached at app startup. This location
         * must be the full url of the JSON file, for example:
         * `"https://turbo.hotwired.dev/demo/json/configuration.json"`
         */
        val remoteFileUrl: String? = null
    )

    /**
     * Loads and parses the specified configuration file(s) from their local
     * and/or remote locations. You should not need to call this directly
     * outside of testing.
     */
    fun load(location: Location) {
        loader.load(location) {
            cachedProperties.clear()
            rules = it.rules
            settings = it.settings
        }
    }

    /**
     * Retrieve the path properties based on the cascading rules in your
     * path configuration.
     *
     * @param location The absolute url to match against the configuration's
     *  rules. Only the url's relative path will be used to find the matching
     *  regex rules.
     * @return The map of key/value `String` properties
     */
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

val TurboPathConfigurationProperties.queryStringPresentation: TurboNavQueryStringPresentation
    @SuppressLint("DefaultLocale") get() = try {
        val value = get("query_string_presentation") ?: "default"
        TurboNavQueryStringPresentation.valueOf(value.toUpperCase())
    } catch (e: IllegalArgumentException) {
        TurboNavQueryStringPresentation.DEFAULT
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