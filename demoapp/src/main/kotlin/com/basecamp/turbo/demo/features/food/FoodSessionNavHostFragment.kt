package com.basecamp.turbo.demo.features.food

import com.basecamp.turbo.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbo.demo.util.Constants

@Suppress("unused")
class FoodSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
