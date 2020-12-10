package com.hotwire.turbo.demo.features.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hotwire.turbo.demo.R
import com.hotwire.turbo.nav.TurbolinksNavGraphDestination

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/web/home")
class WebHomeFragment : WebFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_home, container, false)
    }
}
