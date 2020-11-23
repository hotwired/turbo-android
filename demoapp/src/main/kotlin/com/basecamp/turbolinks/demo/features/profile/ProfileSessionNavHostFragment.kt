package com.basecamp.turbolinks.demo.features.profile

import com.basecamp.turbolinks.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbolinks.demo.util.Constants

@Suppress("unused")
class ProfileSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
