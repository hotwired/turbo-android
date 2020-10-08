package com.basecamp.turbolinks.demo.features.food

import com.basecamp.turbolinks.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbolinks.demo.util.Constants

@Suppress("unused")
class FoodSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "food"
    override val startLocation = Constants.FOOD_URL
}
