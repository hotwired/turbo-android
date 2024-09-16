package dev.hotwire.turbo.config

import android.content.Context
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
            repository.getRemoteConfiguration(url)?.let { remoteConfigJson ->
                repository.parseFromJson(remoteConfigJson)?.let { config ->
                    onCompletion(config)
                    cacheConfigurationForUrl(url, config)
                }
            }
        }
    }

    private fun loadBundledAssetConfiguration(filePath: String, onCompletion: (TurboPathConfiguration) -> Unit) {
        val bundledConfigJson = repository.getBundledConfiguration(context, filePath)
        repository.parseFromJson(bundledConfigJson)?.let { config ->
            onCompletion(config)
        }
    }

    private fun loadCachedConfigurationForUrl(url: String, onCompletion: (TurboPathConfiguration) -> Unit) {
        repository.getCachedConfigurationForUrl(context, url)?.let { cachedConfigJson ->
            repository.parseFromJson(cachedConfigJson)?.let { config ->
                onCompletion(config)
            }
        }
    }

    private fun cacheConfigurationForUrl(url: String, pathConfiguration: TurboPathConfiguration) {
        repository.cacheConfigurationForUrl(context, url, pathConfiguration)
    }
}
