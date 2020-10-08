package com.basecamp.turbolinks.demo

@Suppress("unused")
class FoodSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
