package com.basecamp.turbolinks.demo

@Suppress("unused")
class OrdersNavHostFragment : BaseNavHostFragment() {
    override val turbolinksName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
