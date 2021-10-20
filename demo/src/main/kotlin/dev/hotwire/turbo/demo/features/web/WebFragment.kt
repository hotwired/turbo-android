package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.View
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.util.SIGN_IN_URL
import dev.hotwire.turbo.fragments.TurboWebFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination
import dev.hotwire.turbo.visit.TurboVisitAction.REPLACE
import dev.hotwire.turbo.visit.TurboVisitOptions

@TurboNavGraphDestination(uri = "turbo://fragment/web")
open class WebFragment : TurboWebFragment(), NavDestination {
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

    override fun onVisitErrorReceived(location: String, errorCode: Int) {
        when (errorCode) {
            401 -> navigate(SIGN_IN_URL, TurboVisitOptions(action = REPLACE))
            else -> super.onVisitErrorReceived(location, errorCode)
        }
    }

    private fun setupMenu() {
        toolbarForNavigation()?.inflateMenu(R.menu.web)
    }
}
