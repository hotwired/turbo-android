package com.basecamp.turbolinks.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.basecamp.turbolinks.TurbolinksFragment
import com.basecamp.turbolinks.TurbolinksFragmentObserver
import com.basecamp.turbolinks.TurbolinksView
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_web.*

open class WebFragment : Fragment(), TurbolinksFragment {
    protected val observer by lazy { BridgeFragmentObserver(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(observer)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    // ----------------------------------------------------------------------------
    // TurbolinksFragment interface
    // ----------------------------------------------------------------------------

    override fun onProvideObserver(): TurbolinksFragmentObserver {
        return observer
    }

    override fun onProvideTurbolinksView(): TurbolinksView? {
        return view?.findViewById(R.id.turbolinks_view)
    }

    override fun onProvideErrorPlaceholder(): ViewGroup? {
        return view?.findViewById(R.id.turbolinks_error_placeholder)
    }

    override fun onSetupToolbar() {
        toolbar?.let {
            NavigationUI.setupWithNavController(it, findNavController())
        }
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

    override fun onTitleChanged(title: String) {
        toolbar?.title = title
    }
}
