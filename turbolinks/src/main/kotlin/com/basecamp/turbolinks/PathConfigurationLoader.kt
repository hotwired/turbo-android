package com.basecamp.turbolinks

import android.content.Context
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PathConfigurationLoader(val context: Context) {
    internal var repository = Repository()

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
        loadCachedConfiguration(onCompletion)

        GlobalScope.launch {
            repository.getRemoteConfiguration(url)?.let {
                onCompletion(load(it))
                cacheConfiguration(load(it))
            }
        }
    }

    private fun loadBundledAssetConfiguration(filePath: String, onCompletion: (PathConfiguration) -> Unit) {
        val configuration = repository.getBundledConfiguration(context, filePath)
        onCompletion(load(configuration))
    }

    private fun loadCachedConfiguration(onCompletion: (PathConfiguration) -> Unit) {
        repository.getCachedConfiguration(context)?.let {
            onCompletion(load(it))
        }
    }

    private fun cacheConfiguration(pathConfiguration: PathConfiguration) {
        repository.cacheConfiguration(context, pathConfiguration)
    }

    private fun load(json: String): PathConfiguration {
        return json.toObject(object : TypeToken<PathConfiguration>() {})
    }
}
