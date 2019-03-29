package com.basecamp.turbolinks

import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

@Suppress("unused")
open class TurbolinksFragmentObserver(private val delegate: TurbolinksFragmentDelegate) : LifecycleObserver {

    @OnLifecycleEvent(ON_CREATE)
    private fun create() {
        delegate.onCreate()
    }

    @OnLifecycleEvent(ON_START)
    private fun start() {
        delegate.onStart()
    }

    @OnLifecycleEvent(ON_STOP)
    private fun stop() {
        delegate.onStop()
    }
}
