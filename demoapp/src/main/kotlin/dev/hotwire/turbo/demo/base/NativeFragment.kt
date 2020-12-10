package dev.hotwire.turbo.demo.base

import dev.hotwire.turbo.fragments.TurboFragment

abstract class NativeFragment : TurboFragment(), NavDestination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
