package com.basecamp.turbolinks.demo

import com.basecamp.turbolinks.TurbolinksFragment

abstract class NativeFragment : TurbolinksFragment(), Destination {
    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }
}
