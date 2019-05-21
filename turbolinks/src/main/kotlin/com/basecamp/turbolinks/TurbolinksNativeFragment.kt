package com.basecamp.turbolinks

abstract class TurbolinksNativeFragment : TurbolinksFragment() {
    override fun onStart() {
        super.onStart()
        initToolbar()
    }
}
