package com.basecamp.turbolinks

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

class TurbolinksSharedViewModel : ViewModel() {
    // Modal result can only be retrieved once
    var modalResult: TurbolinksModalResult? = null
        get() = field.also { field = null }

    companion object {
        fun get(activity: FragmentActivity): TurbolinksSharedViewModel {
            return ViewModelProviders.of(activity).get(TurbolinksSharedViewModel::class.java)
        }
    }
}

data class TurbolinksModalResult(
    val location: String,
    val options: VisitOptions
)
