package com.basecamp.turbo.demo.features.profile

import com.basecamp.turbo.demo.base.BaseSessionNavHostFragment
import com.basecamp.turbo.demo.util.Constants

@Suppress("unused")
class ProfileSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
