package com.basecamp.turbolinks

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class TurbolinksNativeFragment : Fragment(), TurbolinksDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }
}
