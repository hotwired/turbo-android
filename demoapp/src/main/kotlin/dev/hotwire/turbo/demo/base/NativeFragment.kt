package dev.hotwire.turbo.demo.base

import dev.hotwire.turbo.fragments.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
