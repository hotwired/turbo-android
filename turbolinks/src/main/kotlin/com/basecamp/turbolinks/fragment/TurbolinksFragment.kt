package com.basecamp.turbolinks.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.core.TurbolinksDestination
import com.basecamp.turbolinks.core.TurbolinksModalResult
import com.basecamp.turbolinks.nav.TurbolinksNavRule.PresentationContext
import com.basecamp.turbolinks.config.context

abstract class TurbolinksFragment : Fragment(), TurbolinksDestination {
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

        if (!viewModel.modalResultExists) {
            delegate.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    open fun onStartAfterModalResult(result: TurbolinksModalResult) {
        delegate.onStartAfterModalResult(result)
    }

    open fun onStartAfterDialogCancel() {
        if (!viewModel.modalResultExists) {
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
        delegate.turbolinksViewModel.modalResult.observe(viewLifecycleOwner) { event ->
            if (shouldHandleModalResults()) {
                event.getContentIfNotHandled()?.let {
                    onStartAfterModalResult(it)
                }
            }
        }
    }

    private fun observeDialogResult() {
        delegate.turbolinksViewModel.dialogResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onStartAfterDialogCancel()
            }
        }
    }

    private fun shouldHandleModalResults(): Boolean {
        // Only handle modal results in non-modal contexts
        return pathProperties.context != PresentationContext.MODAL
    }
}
