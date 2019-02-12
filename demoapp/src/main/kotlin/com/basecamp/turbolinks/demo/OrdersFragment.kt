package com.basecamp.turbolinks.demo

class OrdersFragment : WebFragment() {
    override fun initialUrl(): String {
        return Constants.ORDERS_URL
    }
}
