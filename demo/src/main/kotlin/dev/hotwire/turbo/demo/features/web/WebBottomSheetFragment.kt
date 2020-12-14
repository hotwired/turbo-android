package dev.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.util.Error
import dev.hotwire.turbo.fragments.TurboWebBottomSheetDialogFragment
import dev.hotwire.turbo.fragments.TurboWebFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination
import dev.hotwire.turbo.views.TurboView
import kotlinx.android.synthetic.main.error.view.*

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

    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.progress_bottom_sheet, null)
    }

    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error, null).apply {
            error_message.text = Error(statusCode).message
        }
    }

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    override val turboView: TurboView?
        get() = view?.findViewById(R.id.turbo_view)
}
