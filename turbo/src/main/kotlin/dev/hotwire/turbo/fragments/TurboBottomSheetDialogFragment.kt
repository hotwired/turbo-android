package dev.hotwire.turbo.fragments

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.nav.TurboNavDestination
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.hotwire.turbo.R

/**
 * The base class from which all bottom sheet native fragments in a Turbo driven app
 * should extend from.
 *
 * @constructor Create empty Turbo bottom sheet dialog fragment
 */
abstract class TurboBottomSheetDialogFragment : BottomSheetDialogFragment(),
    TurboNavDestination {
    private lateinit var delegate: TurboFragmentDelegate

    /**
     * Instantiates the [TurboFragmentDelegate].
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurboFragmentDelegate(this)
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
     * Passes this lifecycle call through to [TurboFragmentDelegate.onStart].
     *
     */
    override fun onStart() {
        super.onStart()
        delegate.onStart()
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
     * Passes this call through to [TurboFragmentDelegate.onDialogCancel].
     *
     */
    override fun onCancel(dialog: DialogInterface) {
        delegate.onDialogCancel()
        super.onCancel(dialog)
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

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    /**
     * Returns the delegate instantiated in [onCreate].
     *
     * @return
     */
    override fun delegate(): TurboFragmentDelegate {
        return delegate
    }
}
