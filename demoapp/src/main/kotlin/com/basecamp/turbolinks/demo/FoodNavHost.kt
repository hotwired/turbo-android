package com.basecamp.turbolinks.demo

@Suppress("unused")
class FoodNavHost : BaseNavHost() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
