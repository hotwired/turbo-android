package com.basecamp.turbolinks.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.config.context
import com.basecamp.turbolinks.delegates.TurbolinksFragmentDelegate
import com.basecamp.turbolinks.nav.TurbolinksNavDestination
import com.basecamp.turbolinks.nav.TurbolinksNavPresentationContext
import com.basecamp.turbolinks.session.TurbolinksSessionModalResult

/**
 * The base class from which all "standard" fragments (non-dialogs) in a Turbolinks driven app
 * should extend from.
 *
 * @constructor Create empty Turbolinks fragment
 */
abstract class TurbolinksFragment : Fragment(), TurbolinksNavDestination {
    private lateinit var delegate: TurbolinksFragmentDelegate

    /**
     * Creates the [TurbolinksFragmentDelegate] instance.
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurbolinksFragmentDelegate(this)
    }

    /**
     * Observes 1) modal results and 2) dialog results.
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModalResult()
        observeDialogResult()
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
     * Passes this lifecycle call through to [TurbolinksFragmentDelegate.onStart] if there is no
     * modal result to process.
     *
     */
    override fun onStart() {
        super.onStart()

        if (!sessionViewModel.modalResultExists) {
            delegate.onStart()
        }
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
     * Passes this call through to [TurbolinksFragmentDelegate.onStartAfterModalResult]
     *
     * @param result
     */
    open fun onStartAfterModalResult(result: TurbolinksSessionModalResult) {
        delegate.onStartAfterModalResult(result)
    }

    /**
     * Passes this call through to [TurbolinksFragmentDelegate.onStartAfterDialogCancel] if there
     * is no modal result to process.
     *
     */
    open fun onStartAfterDialogCancel() {
        if (!sessionViewModel.modalResultExists) {
            delegate.onStartAfterDialogCancel()
        }
    }

    /**
     * Implementing classes can execute state cleanup by overriding this. Will always be called
     * before any navigation action takes place.
     *
     */
    override fun onBeforeNavigation() {}

    /**
     * Returns the delegate instantiated in [onCreate].
     *
     * @return
     */
    override fun delegate(): TurbolinksFragmentDelegate {
        return delegate
    }

    private fun observeModalResult() {
        delegate.sessionViewModel.modalResult.observe(viewLifecycleOwner) { event ->
            if (shouldHandleModalResults()) {
                event.getContentIfNotHandled()?.let {
                    onStartAfterModalResult(it)
                }
            }
        }
    }

    private fun observeDialogResult() {
        delegate.sessionViewModel.dialogResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onStartAfterDialogCancel()
            }
        }
    }

    private fun shouldHandleModalResults(): Boolean {
        // Only handle modal results in non-modal contexts
        return pathProperties.context != TurbolinksNavPresentationContext.MODAL
    }
}
