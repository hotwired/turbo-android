package dev.hotwire.turbo.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.context
import dev.hotwire.turbo.config.title
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.nav.TurboNavPresentationContext
import dev.hotwire.turbo.session.TurboSessionModalResult

/**
 * The base class from which all "standard" native fragments (non-dialogs) in a Turbo driven app
 * should extend from.
 *
 * For web fragments, refer to [TurboWebFragment].
 *
 * @constructor Create empty Turbo fragment
 */
abstract class TurboFragment : Fragment(), TurboNavDestination {
    private lateinit var delegate: TurboFragmentDelegate

    /**
     * Instantiates a [TurboFragmentDelegate].
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurboFragmentDelegate(this)
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

        if (shouldObserveTitleChanges()) {
            observeTitleChanges()
            pathProperties.title?.let {
                fragmentViewModel.setTitle(it)
            }
        }
    }

    /**
     * Passes this lifecycle call through to [TurboFragmentDelegate.onActivityCreated].
     *
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    /**
     * Passes this lifecycle call through to [TurboFragmentDelegate.onStart] if there is no
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
     * Passes this lifecycle call through to [TurboFragmentDelegate.onStop].
     *
     */
    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    /**
     * Passes this call through to [TurboFragmentDelegate.onStartAfterModalResult]
     *
     * @param result
     */
    open fun onStartAfterModalResult(result: TurboSessionModalResult) {
        delegate.onStartAfterModalResult(result)
    }

    /**
     * Passes this call through to [TurboFragmentDelegate.onStartAfterDialogCancel] if there
     * is no modal result to process.
     *
     */
    open fun onStartAfterDialogCancel() {
        if (!sessionViewModel.modalResultExists) {
            delegate.onStartAfterDialogCancel()
        }
    }

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
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
    override fun delegate(): TurboFragmentDelegate {
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

    private fun observeTitleChanges() {
        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }

    private fun shouldHandleModalResults(): Boolean {
        // Only handle modal results in non-modal contexts
        return pathProperties.context != TurboNavPresentationContext.MODAL
    }
}
