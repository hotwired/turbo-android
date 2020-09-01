package com.basecamp.turbolinks.demo

@Suppress("unused")
class OrdersNavHost : BaseNavHost() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
