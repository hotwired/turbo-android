package com.basecamp.turbolinks.fragment

import android.content.DialogInterface
import android.os.Bundle
import com.basecamp.turbolinks.nav.TurbolinksNavDestination
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class TurbolinksBottomSheetDialogFragment : BottomSheetDialogFragment(),
    TurbolinksNavDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
    }

    override fun onBeforeNavigation() {
        // Allow subclasses to do state cleanup
    }

    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }
}
