package dev.hotwire.turbo.demo.util

object Constants {
    const val IP_ADDRESS = "x.x.x.x"
    const val BASE_URL = "http://$IP_ADDRESS:9292"
    const val FOOD_URL = BASE_URL
    const val ORDERS_URL = "$BASE_URL/orders"
    const val PROFILE_URL = "$BASE_URL/profile"
    const val PROFILE_EDIT_URL = "$BASE_URL/profile/edit"
    const val AVATAR_URL = "$BASE_URL/images/avatar.png"
}
