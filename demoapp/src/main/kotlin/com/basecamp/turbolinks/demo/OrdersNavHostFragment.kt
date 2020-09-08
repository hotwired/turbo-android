package com.basecamp.turbolinks.demo

@Suppress("unused")
class OrdersNavHostFragment : BaseNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
