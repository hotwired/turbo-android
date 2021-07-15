package dev.hotwire.turbo.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.title
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.nav.TurboNavDestination

/**
 * The base class from which all bottom sheet native fragments in a
 * Turbo-driven app should extend from.
 *
 * For web bottom sheet fragments, refer to [TurboWebBottomSheetDialogFragment].
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
        delegate.onViewCreated()

        if (shouldObserveTitleChanges()) {
            observeTitleChanges()
            pathProperties.title?.let {
                fragmentViewModel.setTitle(it)
            }
        }
    }

    /**
     * This is marked `final` to prevent further use, as it's now deprecated in
     * AndroidX's Fragment implementation.
     *
     * Use [onViewCreated] for code touching
     * the Fragment's view and [onCreate] for other initialization.
     */
    @Suppress("DEPRECATION")
    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    /**
     * This is marked `final` to prevent further use, as it's now deprecated in
     * AndroidX's Fragment implementation.
     *
     * Use [registerForActivityResult] with the appropriate
     * [androidx.activity.result.contract.ActivityResultContract] and its callback.
     *
     * Turbo provides the [TurboNavDestination.activityResultLauncher] interface
     * to obtain registered result launchers from any destination.
     */
    @Suppress("DEPRECATION")
    final override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
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

    override fun onDismiss(dialog: DialogInterface) {
        delegate.onDialogDismiss()
        super.onDismiss(dialog)
    }

    override fun onBeforeNavigation() {}

    override fun refresh(displayProgress: Boolean) {}

    /**
     * Gets the Toolbar instance in your Fragment's view for use with
     * navigation. The title in the Toolbar will automatically be
     * updated if a title is available. By default, Turbo will look
     * for a Toolbar with resource ID `R.id.toolbar`. Override to
     * provide a Toolbar instance with a different ID.
     */
    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    final override fun delegate(): TurboFragmentDelegate {
        return delegate
    }

    private fun observeTitleChanges() {
        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }
}
