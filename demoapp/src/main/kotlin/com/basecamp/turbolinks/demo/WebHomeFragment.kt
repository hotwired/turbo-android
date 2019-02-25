package com.basecamp.turbolinks.demo

import android.view.View

open class WebHomeFragment : WebFragment() {
    override fun onDestinationTitleChanged(title: String) {
        // Do nothing, we don't want to display the title
    }

    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_web_home, null)
    }
}
