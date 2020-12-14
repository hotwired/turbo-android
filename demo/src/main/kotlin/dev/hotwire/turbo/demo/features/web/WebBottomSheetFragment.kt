package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.fragments.TurboWebBottomSheetDialogFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/web/modal/sheet")
open class WebBottomSheetFragment : TurboWebBottomSheetDialogFragment(), NavDestination {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }
}
