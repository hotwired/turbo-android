package com.basecamp.turbolinks.session

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

class TurbolinksSessionViewModel : ViewModel() {
    // Visit options can only be read once
    var visitOptions: TurbolinksSessionEvent<TurbolinksVisitOptions>? = null

    // Modal result can only be observed once
    val modalResult: MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionModalResult>> by lazy {
        MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionModalResult>>()
    }

    val modalResultExists: Boolean
        get() = modalResult.value?.hasBeenHandled == false

    // Dialog result can only be observed once
    val dialogResult: MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionDialogResult>> by lazy {
        MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionDialogResult>>()
    }

    fun saveVisitOptions(options: TurbolinksVisitOptions) {
        visitOptions = TurbolinksSessionEvent(options)
    }

    fun sendModalResult(result: TurbolinksSessionModalResult) {
        modalResult.value = TurbolinksSessionEvent(result)
    }

    fun sendDialogResult() {
        dialogResult.value = TurbolinksSessionEvent(TurbolinksSessionDialogResult(true))
    }

    companion object {
        fun get(sessionName: String, activity: FragmentActivity): TurbolinksSessionViewModel {
            return ViewModelProvider(activity).get(
                sessionName, TurbolinksSessionViewModel::class.java
            )
        }
    }
}
