package com.hotwire.turbo.demo.features.food

import com.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import com.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class FoodSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
