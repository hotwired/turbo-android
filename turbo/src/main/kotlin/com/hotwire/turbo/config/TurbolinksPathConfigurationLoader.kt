package com.hotwire.turbo.config

import android.content.Context
import com.hotwire.turbo.util.coroutineScope
import com.hotwire.turbo.util.toObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

internal class TurbolinksPathConfigurationLoader(val context: Context) {
    internal var repository = TurbolinksPathConfigurationRepository()

    fun load(location: TurbolinksPathConfiguration.Location, onCompletion: (TurbolinksPathConfiguration) -> Unit) {
        location.assetFilePath?.let {
            loadBundledAssetConfiguration(it, onCompletion)
        }

        location.remoteFileUrl?.let {
            downloadRemoteConfiguration(it, onCompletion)
        }
    }

    private fun downloadRemoteConfiguration(url: String, onCompletion: (TurbolinksPathConfiguration) -> Unit) {
        // Always load the previously cached version first, if available
        loadCachedConfigurationForUrl(url, onCompletion)

        context.coroutineScope().launch {
            repository.getRemoteConfiguration(url)?.let {
                onCompletion(load(it))
                cacheConfigurationForUrl(url, load(it))
            }
        }
    }

    private fun loadBundledAssetConfiguration(filePath: String, onCompletion: (TurbolinksPathConfiguration) -> Unit) {
        val configuration = repository.getBundledConfiguration(context, filePath)
        onCompletion(load(configuration))
    }

    private fun loadCachedConfigurationForUrl(url: String, onCompletion: (TurbolinksPathConfiguration) -> Unit) {
        repository.getCachedConfigurationForUrl(context, url)?.let {
            onCompletion(load(it))
        }
    }

    private fun cacheConfigurationForUrl(url: String, pathConfiguration: TurbolinksPathConfiguration) {
        repository.cacheConfigurationForUrl(context, url, pathConfiguration)
    }

    private fun load(json: String): TurbolinksPathConfiguration {
        return json.toObject(object : TypeToken<TurbolinksPathConfiguration>() {})
    }
}
