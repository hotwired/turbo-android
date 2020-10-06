package com.basecamp.turbolinks.demo

@Suppress("unused")
class ProfileNavHostFragment : BaseNavHostFragment() {
    override val turbolinksName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
