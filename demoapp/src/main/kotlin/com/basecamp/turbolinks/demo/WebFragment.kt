package com.basecamp.turbolinks.demo

import android.view.View

open class WebFragment : BridgeFragment(), NavigationFragment {
    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_web, null)
    }

    override fun provideTitle(): String {
        return title()
    }
}
