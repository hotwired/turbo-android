package com.basecamp.turbolinks.demo

import android.view.View

class OrdersFragment : BridgeFragment(), NavigationFragment {
    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_web, null)
    }

    override fun initialUrl(): String {
        return Constants.ORDERS_URL
    }

    override fun provideTitle(): String {
        return title()
    }
}
