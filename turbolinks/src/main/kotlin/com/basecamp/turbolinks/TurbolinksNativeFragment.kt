package com.basecamp.turbolinks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.basecamp.turbolinks.TurbolinksNavigator.PresentationContext

abstract class TurbolinksNativeFragment : Fragment(), TurbolinksDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModalResult()
        observeDialogResult()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()

        if (!sessionViewModel.modalResultExists) {
            delegate.onStart()
        }
    }

    open fun onStartAfterModalResult(result: TurbolinksModalResult) {
        delegate.onStartAfterModalResult(result)
    }

    open fun onStartAfterDialogCancel() {
        if (!sessionViewModel.modalResultExists) {
            delegate.onStartAfterDialogCancel()
        }
    }

    override fun onBeforeNavigation() {
        // Allow subclasses to do state cleanup
    }

    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }

    private fun observeModalResult() {
        delegate.sessionViewModel.modalResult.observe(viewLifecycleOwner, Observer { event ->
            // Only handle modal results in non-modal contexts
            if (pathProperties.context != PresentationContext.MODAL) {
                event.getContentIfNotHandled()?.let {
                    onStartAfterModalResult(it)
                }
            }
        })
    }

    private fun observeDialogResult() {
        delegate.sessionViewModel.dialogResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                onStartAfterDialogCancel()
            }
        })
    }
}
