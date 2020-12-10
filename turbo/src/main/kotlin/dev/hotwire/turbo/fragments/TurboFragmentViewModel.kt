package dev.hotwire.turbo.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Holds onto fragment-level state data.
 *
 * @constructor Create empty Turbo fragment view model
 */
class TurboFragmentViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()

    /**
     * Set's the page title.
     *
     * @param newTitle
     */
    fun setTitle(newTitle: String) {
        title.value = newTitle
    }

    companion object {
        fun get(location: String, fragment: Fragment): TurboFragmentViewModel {
            return ViewModelProvider(fragment).get(
                location, TurboFragmentViewModel::class.java
            )
        }
    }
}
