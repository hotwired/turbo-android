package dev.hotwire.turbo.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.nav.TurboNavDestination
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.title

/**
 * The base class from which all bottom sheet native fragments in a Turbo driven app
 * should extend from.
 *
 * @constructor Create empty Turbo bottom sheet dialog fragment
 */
abstract class TurboBottomSheetDialogFragment : BottomSheetDialogFragment(),
    TurboNavDestination {
    internal lateinit var delegate: TurboFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurboFragmentDelegate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (shouldObserveTitleChanges()) {
            observeTitleChanges()
            pathProperties.title?.let {
                fragmentViewModel.setTitle(it)
            }
        }
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

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
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

    private fun observeTitleChanges() {
        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }
}
