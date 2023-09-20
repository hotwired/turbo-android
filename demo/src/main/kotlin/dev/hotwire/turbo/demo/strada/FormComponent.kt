package dev.hotwire.turbo.demo.strada

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.databinding.FormComponentSubmitBinding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component to display a submit button in the native toolbar,
 * which will submit the form on the page when tapped.
 */
class FormComponent(
    name: String,
    private val delegate: BridgeDelegate<NavDestination>
) : BridgeComponent<NavDestination>(name, delegate) {

    private val submitButtonItemId = 37
    private var submitMenuItem: MenuItem? = null
    private val fragment: Fragment
        get() = delegate.destination.fragment
    private val toolbar: Toolbar?
        get() = fragment.view?.findViewById(R.id.toolbar)

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> handleConnectEvent(message)
            "submitEnabled" -> handleSubmitEnabled()
            "submitDisabled" -> handleSubmitDisabled()
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
    }

    private fun handleConnectEvent(message: Message) {
        val data = message.data<MessageData>() ?: return
        showToolbarButton(data)
    }

    private fun handleSubmitEnabled() {
        toggleSubmitButton(true)
    }

    private fun handleSubmitDisabled() {
        toggleSubmitButton(false)
    }

    private fun showToolbarButton(data: MessageData) {
        val menu = toolbar?.menu ?: return
        val inflater = LayoutInflater.from(fragment.requireContext())
        val binding = FormComponentSubmitBinding.inflate(inflater)
        val order = 999 // Show as the right-most button

        binding.formSubmit.apply {
            text = data.title
            setOnClickListener {
                performSubmit()
            }
        }

        menu.removeItem(submitButtonItemId)
        submitMenuItem = menu.add(Menu.NONE, submitButtonItemId, order, data.title).apply {
            actionView = binding.root
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    private fun toggleSubmitButton(enable: Boolean) {
        val layout = submitMenuItem?.actionView ?: return

        FormComponentSubmitBinding.bind(layout).apply {
            formSubmit.isEnabled = enable
        }
    }

    private fun performSubmit(): Boolean {
        return replyTo("connect")
    }

    @Serializable
    data class MessageData(
        @SerialName("submitTitle") val title: String
    )
}
