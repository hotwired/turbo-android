package com.basecamp.turbolinks

import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class TurbolinksNativeBottomSheetFragment : BottomSheetDialogFragment(), TurbolinksDestination {
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

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }
}
