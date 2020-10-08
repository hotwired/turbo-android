package com.basecamp.turbolinks.demo.features.orders

import com.basecamp.turbolinks.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbolinks.demo.util.Constants

@Suppress("unused")
class OrdersSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
