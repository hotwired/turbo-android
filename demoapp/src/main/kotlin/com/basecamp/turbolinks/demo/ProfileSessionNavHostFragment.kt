package com.basecamp.turbolinks.demo

@Suppress("unused")
class ProfileSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
