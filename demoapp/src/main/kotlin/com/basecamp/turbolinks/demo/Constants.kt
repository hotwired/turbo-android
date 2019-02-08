package com.basecamp.turbolinks.demo

object Constants {
    const val IP_ADDRESS = "10.0.1.22"
    const val BASE_URL = "http://$IP_ADDRESS:9292"
    const val FOOD_URL = BASE_URL
    const val ORDERS_URL = "$BASE_URL/orders"
    const val PROFILE_EDIT_URL = "$BASE_URL/profile/edit"
    const val AVATAR_URL = "$BASE_URL/images/avatar.png"
}
