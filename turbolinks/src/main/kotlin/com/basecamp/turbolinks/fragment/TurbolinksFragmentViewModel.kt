package com.basecamp.turbolinks.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TurbolinksFragmentViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()

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
