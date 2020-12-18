package dev.hotwire.turbo.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Holds onto fragment-level state data.
 */
class TurboFragmentViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()

    /**
     * Set's the Fragment destination's title.
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
