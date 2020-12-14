package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.View
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.util.drawable
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/web/modal")
class WebModalFragment : WebFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
    }

    private fun initToolbar() {
        toolbarForNavigation()?.navigationIcon =  context?.drawable(R.drawable.ic_close)
    }
}
