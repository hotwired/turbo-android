package com.basecamp.turbolinks.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Holds onto fragment-level state data.
 *
 * @constructor Create empty Turbolinks fragment view model
 */
class TurbolinksFragmentViewModel : ViewModel() {
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
        fun get(location: String, fragment: Fragment): TurbolinksFragmentViewModel {
            return ViewModelProvider(fragment).get(
                location, TurbolinksFragmentViewModel::class.java
            )
        }
    }
}
