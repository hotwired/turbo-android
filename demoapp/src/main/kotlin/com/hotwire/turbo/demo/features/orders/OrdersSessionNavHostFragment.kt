package com.hotwire.turbo.demo.features.orders

import com.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import com.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class OrdersSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
