package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.hotwire.turbo.demo.R
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_bottom_sheet, container, false)
    }
}
