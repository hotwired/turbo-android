package dev.hotwire.turbo.demo.strada

import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.databinding.MenuComponentBottomSheetBinding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component to display a native bottom sheet menu, which will
 * send the selected index of the tapped menu item back to the web.
 */
class MenuComponent(
    name: String,
    private val delegate: BridgeDelegate<NavDestination>
) : BridgeComponent<NavDestination>(name, delegate) {

    private val fragment: Fragment
        get() = delegate.destination.fragment

    override fun onReceive(message: Message) {
        when (message.event) {
            "display" -> handleDisplayEvent(message)
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
    }

    private fun handleDisplayEvent(message: Message) {
        val data = message.data<MessageData>() ?: return
        showBottomSheet(data.title, data.items)
    }

    private fun showBottomSheet(title: String, items: List<Item>) {
        val view = fragment.view?.rootView ?: return
        val inflater = LayoutInflater.from(view.context)
        val bottomSheet = BottomSheetDialog(view.context)
        val binding = MenuComponentBottomSheetBinding.inflate(inflater)

        binding.toolbar.title = title
        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        binding.recyclerView.adapter = MenuComponentAdapter().apply {
            setData(items)
            setListener {
                bottomSheet.dismiss()
                onItemSelected(it)
            }
        }

        bottomSheet.apply {
            setContentView(binding.root)
            show()
        }
    }

    private fun onItemSelected(item: Item) {
        replyTo("display", SelectionMessageData(item.index))
    }

    @Serializable
    data class MessageData(
        @SerialName("title") val title: String,
        @SerialName("items") val items: List<Item>
    )

    @Serializable
    data class Item(
        @SerialName("title") val title: String,
        @SerialName("index") val index: Int
    )

    @Serializable
    data class SelectionMessageData(
        @SerialName("selectedIndex") val selectedIndex: Int
    )
}
