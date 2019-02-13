package com.basecamp.turbolinks.demosimple

import android.view.View
import com.basecamp.turbolinks.TurbolinksFragment

class WebFragment : TurbolinksFragment(), NavigationFragment {
    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_web, null)
    }

    override fun provideTitle(): String {
        return title()
    }
}
