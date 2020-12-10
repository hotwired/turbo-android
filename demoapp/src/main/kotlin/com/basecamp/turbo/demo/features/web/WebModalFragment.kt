package com.basecamp.turbo.demo.features.web

import android.os.Bundle
import com.basecamp.turbo.demo.R
import com.basecamp.turbo.demo.extensions.drawable
import com.basecamp.turbo.nav.TurbolinksNavGraphDestination

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/web/modal")
class WebModalFragment : WebFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
    }

    override fun displaysBottomTabs(): Boolean {
        return false
    }

    private fun initToolbar() {
        toolbarForNavigation()?.navigationIcon = context?.drawable(R.drawable.ic_close)
        toolbarForNavigation()?.navigationContentDescription = getString(R.string.modal_close)
    }
}
