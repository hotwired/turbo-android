package dev.hotwire.turbo.demo.features.food

import dev.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import dev.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class FoodSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
