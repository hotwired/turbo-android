package com.basecamp.turbolinks

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TurbolinksSessionViewModel : ViewModel() {
    // Modal result can only be retrieved once
    var modalResult: TurbolinksModalResult? = null
        get() = field.also { field = null }

    // Dialog dismissed event can only be observed once
    val dialogResult: MutableLiveData<TurbolinksEvent<TurbolinksDialogResult>> by lazy {
        MutableLiveData<TurbolinksEvent<TurbolinksDialogResult>>()
    }

    fun sendDialogResult() {
        dialogResult.value = TurbolinksEvent(TurbolinksDialogResult(true))
    }

    companion object {
        fun get(sessionName: String, activity: FragmentActivity): TurbolinksSessionViewModel {
            return ViewModelProvider(activity).get(
                sessionName, TurbolinksSessionViewModel::class.java
            )
        }
    }
}

data class TurbolinksModalResult(
    val location: String,
    val options: VisitOptions
)

data class TurbolinksDialogResult(
    val dismissed: Boolean
)
