package com.basecamp.turbolinks

import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

@Suppress("unused")
open class TurbolinksFragmentObserver(private val delegate: TurbolinksFragmentDelegate) : LifecycleObserver {

    @OnLifecycleEvent(ON_CREATE)
    private fun create() {
        val location = delegate.fragment.arguments?.getString("location") ?:
                throw IllegalArgumentException("A location argument must be provided")

        delegate.onCreate(location)
    }

    @OnLifecycleEvent(ON_START)
    private fun start() {
        val activity = delegate.fragment.context as? TurbolinksActivity ?:
                throw RuntimeException("The fragment Activity must implement TurbolinksActivity")

        delegate.onStart(activity)
    }

    @OnLifecycleEvent(ON_STOP)
    private fun stop() {
        delegate.onStop()
    }
}
