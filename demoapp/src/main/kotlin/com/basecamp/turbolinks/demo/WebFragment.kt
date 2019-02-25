package com.basecamp.turbolinks.demo

import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.fragment_web.*

open class WebFragment : BridgeFragment() {
    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_web, null)
    }

    override fun onStart() {
        super.onStart()
        setupToolbar()
    }

    override fun onDestinationTitleChanged(title: String) {
        updateToolbarTitle(title)
    }

    private fun setupToolbar() {
        toolbar?.let {
            NavigationUI.setupWithNavController(it, findNavController())
        }
    }

    private fun updateToolbarTitle(title: String) {
        toolbar?.title = title
    }
}
