package com.basecamp.turbo.demo.features.orders

import com.basecamp.turbo.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbo.demo.util.Constants

@Suppress("unused")
class OrdersSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
