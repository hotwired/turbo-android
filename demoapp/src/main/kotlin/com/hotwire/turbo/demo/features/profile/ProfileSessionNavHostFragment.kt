package com.hotwire.turbo.demo.features.profile

import com.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import com.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class ProfileSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
