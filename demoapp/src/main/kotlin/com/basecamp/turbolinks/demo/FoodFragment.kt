package com.basecamp.turbolinks.demo

class FoodFragment : WebFragment() {
    override fun initialUrl(): String {
        return Constants.FOOD_URL
    }
}
