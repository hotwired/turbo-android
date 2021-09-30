package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.View
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.fragments.TurboWebBottomSheetDialogFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/web/modal/sheet")
class WebBottomSheetFragment : TurboWebBottomSheetDialogFragment(), NavDestination {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }

    override fun onFormSubmissionStarted(location: String) {
        menuProgress?.isVisible = true
    }

    override fun onFormSubmissionFinished(location: String) {
        menuProgress?.isVisible = false
    }

    private fun setupMenu() {
        toolbarForNavigation()?.inflateMenu(R.menu.web)
    }
}
