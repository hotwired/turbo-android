package com.basecamp.turbolinks.demo

@Suppress("unused")
class FoodNavHostFragment : BaseNavHostFragment() {
    override val turbolinksName = "food"
    override val startLocation = Constants.FOOD_URL
}
