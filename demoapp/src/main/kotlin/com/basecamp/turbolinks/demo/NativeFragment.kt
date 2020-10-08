package com.basecamp.turbolinks.demo

import com.basecamp.turbolinks.fragments.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
