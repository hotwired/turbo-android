package com.basecamp.turbo.demo.base

import com.basecamp.turbo.fragments.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
