package com.basecamp.turbolinks.session

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basecamp.turbolinks.visit.TurbolinksVisitOptions

/**
 * Serves as a shared ViewModel to exchange data between [TurbolinksSession] and various other
 * internal classes. Typically used to share navigational events.
 *
 * @constructor
 */
class TurbolinksSessionViewModel : ViewModel() {
    /**
     * Represents visit options for the current visit. Typically consumed by a delegate to execute
     * a navigation action. Can only be consumed once.
     */
    var visitOptions: TurbolinksSessionEvent<TurbolinksVisitOptions>? = null
        private set

    /**
     * A one-time event that can be observed to determine if a closing modal has returned a result
     * to be proceed. Can only be consumed once.
     */
    val modalResult: MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionModalResult>> by lazy {
        MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionModalResult>>()
    }

    /**
     * Convenience method to check if the modal result has already been consumed.
     */
    val modalResultExists: Boolean
        get() = modalResult.value?.hasBeenHandled == false

    // Dialog result can only be observed once
    val dialogResult: MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionDialogResult>> by lazy {
        MutableLiveData<TurbolinksSessionEvent<TurbolinksSessionDialogResult>>()
    }

    /**
     * Wraps the visit options in a [TurbolinksSessionEvent] to ensure it can only be consumed once.
     *
     * @param options
     */
    fun saveVisitOptions(options: TurbolinksVisitOptions) {
        visitOptions = TurbolinksSessionEvent(options)
    }

    /**
     * Wraps a modal result in a [TurbolinksSessionEvent] and updates the LiveData value.
     *
     * @param result
     */
    fun sendModalResult(result: TurbolinksSessionModalResult) {
        modalResult.value = TurbolinksSessionEvent(result)
    }

    /**
     * Wraps a dialog result in a [TurbolinksSessionEvent] and updates the LiveData value.
     *
     */
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
