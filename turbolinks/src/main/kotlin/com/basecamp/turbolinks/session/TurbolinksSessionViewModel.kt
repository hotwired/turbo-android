package com.basecamp.turbolinks.session

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions
import com.basecamp.turbolinks.util.TurbolinksEvent

class TurbolinksSessionViewModel : ViewModel() {
    // Visit options can only be read once
    var visitOptions: TurbolinksEvent<TurbolinksVisitOptions>? = null

    // Modal result can only be observed once
    val modalResult: MutableLiveData<TurbolinksEvent<TurbolinksSessionModalResult>> by lazy {
        MutableLiveData<TurbolinksEvent<TurbolinksSessionModalResult>>()
    }

    val modalResultExists: Boolean
        get() = modalResult.value?.hasBeenHandled == false

    // Dialog result can only be observed once
    val dialogResult: MutableLiveData<TurbolinksEvent<TurbolinksSessionDialogResult>> by lazy {
        MutableLiveData<TurbolinksEvent<TurbolinksSessionDialogResult>>()
    }

    fun saveVisitOptions(options: TurbolinksVisitOptions) {
        visitOptions = TurbolinksEvent(options)
    }

    fun sendModalResult(result: TurbolinksSessionModalResult) {
        modalResult.value = TurbolinksEvent(result)
    }

    fun sendDialogResult() {
        dialogResult.value = TurbolinksEvent(TurbolinksSessionDialogResult(true))
    }

    companion object {
        fun get(sessionName: String, activity: FragmentActivity): TurbolinksSessionViewModel {
            return ViewModelProvider(activity).get(
                sessionName, TurbolinksSessionViewModel::class.java
            )
        }
    }
}
