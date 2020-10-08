package com.basecamp.turbolinks.demo

@Suppress("unused")
class OrdersSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
