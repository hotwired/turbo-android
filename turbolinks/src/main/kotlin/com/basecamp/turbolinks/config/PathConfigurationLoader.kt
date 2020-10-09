package com.basecamp.turbolinks.config

import android.content.Context
import com.basecamp.turbolinks.util.coroutineScope
import com.basecamp.turbolinks.util.toObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

internal class PathConfigurationLoader(val context: Context) {
    var repository = PathConfigurationRepository()

    fun load(location: PathConfiguration.Location, onCompletion: (PathConfiguration) -> Unit) {
        location.assetFilePath?.let {
            loadBundledAssetConfiguration(it, onCompletion)
        }

        location.remoteFileUrl?.let {
            downloadRemoteConfiguration(it, onCompletion)
        }
    }

    private fun downloadRemoteConfiguration(url: String, onCompletion: (PathConfiguration) -> Unit) {
        // Always load the previously cached version first, if available
        loadCachedConfigurationForUrl(url, onCompletion)

        context.coroutineScope().launch {
            repository.getRemoteConfiguration(url)?.let {
                onCompletion(load(it))
                cacheConfigurationForUrl(url, load(it))
            }
        }
    }

    private fun loadBundledAssetConfiguration(filePath: String, onCompletion: (PathConfiguration) -> Unit) {
        val configuration = repository.getBundledConfiguration(context, filePath)
        onCompletion(load(configuration))
    }

    private fun loadCachedConfigurationForUrl(url: String, onCompletion: (PathConfiguration) -> Unit) {
        repository.getCachedConfigurationForUrl(context, url)?.let {
            onCompletion(load(it))
        }
    }

    private fun cacheConfigurationForUrl(url: String, pathConfiguration: PathConfiguration) {
        repository.cacheConfigurationForUrl(context, url, pathConfiguration)
    }

    private fun load(json: String): PathConfiguration {
        return json.toObject(object : TypeToken<PathConfiguration>() {})
    }
}
