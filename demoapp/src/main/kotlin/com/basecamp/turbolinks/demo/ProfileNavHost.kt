package com.basecamp.turbolinks.demo

@Suppress("unused")
class ProfileNavHost : BaseNavHost() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
