package dev.hotwire.turbo.demo.strada

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.hotwire.turbo.demo.R

class MenuComponentAdapter : RecyclerView.Adapter<MenuComponentAdapter.ViewHolder>() {
    private val type = R.layout.menu_component_adapter_row
    private var action: ((MenuComponent.Item) -> Unit)? = null

    private var items = emptyList<MenuComponent.Item>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setData(items: List<MenuComponent.Item>) {
        this.items = items
    }

    fun setListener(action: (item: MenuComponent.Item) -> Unit) {
        this.action = action
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun getItemViewType(position: Int): Int {
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: MaterialTextView = view.findViewById(R.id.title)

        fun bind(item: MenuComponent.Item) {
            textView.text = item.title
            itemView.setOnClickListener {
                action?.invoke(item)
            }
        }
    }
}
