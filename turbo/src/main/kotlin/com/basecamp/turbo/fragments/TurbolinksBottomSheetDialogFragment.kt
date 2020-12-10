package com.basecamp.turbo.fragments

import android.content.DialogInterface
import android.os.Bundle
import com.basecamp.turbo.delegates.TurbolinksFragmentDelegate
import com.basecamp.turbo.nav.TurbolinksNavDestination
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * The base class from which all bottom sheet native fragments in a Turbolinks driven app
 * should extend from.
 *
 * @constructor Create empty Turbolinks bottom sheet dialog fragment
 */
abstract class TurbolinksBottomSheetDialogFragment : BottomSheetDialogFragment(),
    TurbolinksNavDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    /**
     * Instantiates the [TurbolinksFragmentDelegate].
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this)
    }

    /**
     * Passes this lifecycle call through to [TurbolinksFragmentDelegate.onActivityCreated].
     *
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    /**
     * Passes this lifecycle call through to [TurbolinksFragmentDelegate.onStart].
     *
     */
    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    /**
     * Passes this lifecycle call through to [TurbolinksFragmentDelegate.onStop].
     *
     */
    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    /**
     * Passes this call through to [TurbolinksFragmentDelegate.onDialogCancel].
     *
     */
    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
    }

    /**
     * Implementing classes can execute state cleanup by overriding this. Will always be called
     * before any navigation action takes place.
     *
     */
    override fun onBeforeNavigation() {}

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    /**
     * Returns the delegate instantiated in [onCreate].
     *
     * @return
     */
    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }
}
