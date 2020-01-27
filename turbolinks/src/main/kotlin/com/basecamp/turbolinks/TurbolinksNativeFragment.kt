package com.basecamp.turbolinks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

abstract class TurbolinksNativeFragment : Fragment(), TurbolinksDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDialogResult()
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

    open fun onStartAfterDialogDismiss() {
        delegate.onStart()
    }

    private fun observeDialogResult() {
        delegate.sessionViewModel.dialogResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                onStartAfterDialogDismiss()
            }
        })
    }
}
