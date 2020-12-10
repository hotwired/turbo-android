package com.hotwire.turbo.demo.base

import com.hotwire.turbo.fragments.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
