package dev.hotwire.turbo.config

import android.content.Context
import dev.hotwire.turbo.util.toObject
import com.google.gson.reflect.TypeToken
import dev.hotwire.turbo.util.dispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class TurboPathConfigurationLoader(val context: Context) : CoroutineScope {
    internal var repository = TurboPathConfigurationRepository()

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io + Job()

    fun load(location: TurboPathConfiguration.Location, onCompletion: (TurboPathConfiguration) -> Unit) {
        location.assetFilePath?.let {
            loadBundledAssetConfiguration(it, onCompletion)
        }

        location.remoteFileUrl?.let {
            downloadRemoteConfiguration(it, onCompletion)
        }
    }

    private fun downloadRemoteConfiguration(url: String, onCompletion: (TurboPathConfiguration) -> Unit) {
        // Always load the previously cached version first, if available
        loadCachedConfigurationForUrl(url, onCompletion)

        launch {
            repository.getRemoteConfiguration(url)?.let {
                onCompletion(load(it))
                cacheConfigurationForUrl(url, load(it))
            }
        }
    }

    private fun loadBundledAssetConfiguration(filePath: String, onCompletion: (TurboPathConfiguration) -> Unit) {
        val configuration = repository.getBundledConfiguration(context, filePath)
        onCompletion(load(configuration))
    }

    private fun loadCachedConfigurationForUrl(url: String, onCompletion: (TurboPathConfiguration) -> Unit) {
        repository.getCachedConfigurationForUrl(context, url)?.let {
            onCompletion(load(it))
        }
    }

    private fun cacheConfigurationForUrl(url: String, pathConfiguration: TurboPathConfiguration) {
        repository.cacheConfigurationForUrl(context, url, pathConfiguration)
    }

    private fun load(json: String): TurboPathConfiguration {
        return json.toObject(object : TypeToken<TurboPathConfiguration>() {})
    }
}
