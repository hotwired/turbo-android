package com.basecamp.turbolinks.demo

import com.basecamp.turbolinks.fragment.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
