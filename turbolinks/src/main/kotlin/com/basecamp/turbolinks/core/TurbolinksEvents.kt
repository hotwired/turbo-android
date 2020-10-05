package com.basecamp.turbolinks.core

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basecamp.turbolinks.util.TurbolinksEvent

class TurbolinksEvents : ViewModel() {
    // Visit options can only be read once
    var visitOptions: TurbolinksEvent<VisitOptions>? = null

    // Modal result can only be observed once
    val modalResult: MutableLiveData<TurbolinksEvent<TurbolinksModalResult>> by lazy {
        MutableLiveData<TurbolinksEvent<TurbolinksModalResult>>()
    }

    val modalResultExists: Boolean
        get() = modalResult.value?.hasBeenHandled == false

    // Dialog result can only be observed once
    val dialogResult: MutableLiveData<TurbolinksEvent<TurbolinksDialogResult>> by lazy {
        MutableLiveData<TurbolinksEvent<TurbolinksDialogResult>>()
    }

    fun saveVisitOptions(options: VisitOptions) {
        visitOptions = TurbolinksEvent(options)
    }

    fun sendModalResult(result: TurbolinksModalResult) {
        modalResult.value = TurbolinksEvent(result)
    }

    fun sendDialogResult() {
        dialogResult.value = TurbolinksEvent(TurbolinksDialogResult(true))
    }

    companion object {
        fun get(sessionName: String, activity: FragmentActivity): TurbolinksEvents {
            return ViewModelProvider(activity).get(
                sessionName, TurbolinksEvents::class.java
            )
        }
    }
}

data class TurbolinksModalResult(
        val location: String,
        val options: VisitOptions,
        val bundle: Bundle?,
        val shouldNavigate: Boolean
)

data class TurbolinksDialogResult(
    val cancelled: Boolean
)
