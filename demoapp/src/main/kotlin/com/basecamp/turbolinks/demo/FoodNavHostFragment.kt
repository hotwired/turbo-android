package com.basecamp.turbolinks.demo

@Suppress("unused")
class FoodNavHostFragment : BaseNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
