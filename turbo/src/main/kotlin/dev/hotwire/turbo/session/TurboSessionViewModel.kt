package dev.hotwire.turbo.session

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * Serves as a shared ViewModel to exchange data between [TurboSession] and various other
 * internal classes. Typically used to share navigational events.
 */
internal class TurboSessionViewModel : ViewModel() {
    /**
     * Represents visit options for the current visit. Typically consumed by a delegate to execute
     * a navigation action. Can only be consumed once.
     */
    var visitOptions: TurboSessionEvent<TurboVisitOptions>? = null
        private set

    /**
     * A one-time event that can be observed to determine if a closing modal has returned a result
     * to be proceed. Can only be consumed once.
     */
    val modalResult: MutableLiveData<TurboSessionEvent<TurboSessionModalResult>> by lazy {
        MutableLiveData<TurboSessionEvent<TurboSessionModalResult>>()
    }

    /**
     * Convenience method to check if the modal result has already been consumed.
     */
    val modalResultExists: Boolean
        get() = modalResult.value?.hasBeenHandled == false

    /**
     * A one-time event that can be observed to determine when a dialog has been cancelled.
     */
    val dialogResult: MutableLiveData<TurboSessionEvent<TurboSessionDialogResult>> by lazy {
        MutableLiveData<TurboSessionEvent<TurboSessionDialogResult>>()
    }

    /**
     * Wraps the visit options in a [TurboSessionEvent] to ensure it can only be consumed once.
     */
    fun saveVisitOptions(options: TurboVisitOptions) {
        visitOptions = TurboSessionEvent(options)
    }

    /**
     * Wraps a modal result in a [TurboSessionEvent] and updates the LiveData value.
     */
    fun sendModalResult(result: TurboSessionModalResult) {
        modalResult.value = TurboSessionEvent(result)
    }

    /**
     * Wraps a dialog result in a [TurboSessionEvent] and updates the LiveData value.
     */
    fun sendDialogResult() {
        dialogResult.value = TurboSessionEvent(TurboSessionDialogResult(true))
    }

    companion object {
        fun get(sessionName: String, activity: FragmentActivity): TurboSessionViewModel {
            return ViewModelProvider(activity).get(
                sessionName, TurboSessionViewModel::class.java
            )
        }
    }
}
