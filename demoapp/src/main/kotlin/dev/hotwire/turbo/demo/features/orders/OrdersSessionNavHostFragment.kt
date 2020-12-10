package dev.hotwire.turbo.demo.features.orders

import dev.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import dev.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class OrdersSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "orders"
    override val startLocation = Constants.ORDERS_URL
}
