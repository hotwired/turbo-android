package dev.hotwire.turbo.demo.strada

import android.util.Log
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import dev.hotwire.turbo.demo.base.NavDestination

class FormComponent(
    name: String,
    delegate: BridgeDelegate<NavDestination>
) : BridgeComponent<NavDestination>(name, delegate) {

    override fun onReceive(message: Message) {
        Log.d("Demo", "FormComponent message received: $message")
    }
}
