package dev.hotwire.turbo.demo.features.profile

import dev.hotwire.turbo.demo.base.BaseSessionNavHostFragment
import dev.hotwire.turbo.demo.util.Constants

@Suppress("unused")
class ProfileSessionNavHostFragment : BaseSessionNavHostFragment() {
    override val sessionName = "profile"
    override val startLocation = Constants.PROFILE_URL
}
