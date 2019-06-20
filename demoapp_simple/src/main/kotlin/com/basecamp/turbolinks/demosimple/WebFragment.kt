package com.basecamp.turbolinks.demosimple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.basecamp.turbolinks.TurbolinksView
import com.basecamp.turbolinks.TurbolinksWebFragment
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_web.*

open class WebFragment : TurbolinksWebFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onProvideToolbar(): Toolbar? {
        return toolbar
    }

    // ----------------------------------------------------------------------------
    // TurbolinksWebFragmentCallback interface
    // ----------------------------------------------------------------------------

    override fun onProvideTurbolinksView(): TurbolinksView? {
        return view?.findViewById(R.id.turbolinks_view)
    }

    override fun onProvideErrorPlaceholder(): ViewGroup? {
        return view?.findViewById(R.id.turbolinks_error_placeholder)
    }

    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.progress, null)
    }

    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error, null).apply {
            error_message.text = Error.getMessage(statusCode)
        }
    }

    override fun shouldEnablePullToRefresh(): Boolean {
        return true
    }
}
