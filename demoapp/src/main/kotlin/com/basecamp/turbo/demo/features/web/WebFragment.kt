package com.basecamp.turbo.demo.features.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.basecamp.turbo.demo.util.Error
import com.basecamp.turbo.demo.base.NavDestination
import com.basecamp.turbo.demo.R
import com.basecamp.turbo.nav.TurbolinksNavGraphDestination
import com.basecamp.turbo.fragments.TurbolinksWebFragment
import com.basecamp.turbo.views.TurbolinksView
import kotlinx.android.synthetic.main.error.view.*

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/web")
open class WebFragment : TurbolinksWebFragment(), NavDestination {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }

    override fun onResume() {
        super.onResume()
        animateBottomNavVisibility()
    }

    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.progress, null)
    }

    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error, null).apply {
            error_message.text = Error.getMessage(statusCode)
        }
    }

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    override val turbolinksView: TurbolinksView?
        get() = view?.findViewById(R.id.turbolinks_view)
}
