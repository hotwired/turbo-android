package com.basecamp.turbolinks

import android.os.Bundle
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

abstract class TurbolinksFragment : Fragment(), TurbolinksFragmentCallback {
    private lateinit var location: String
    private lateinit var delegate: TurbolinksFragmentDelegate

    lateinit var sharedViewModel: TurbolinksSharedViewModel
    lateinit var viewModel: TurbolinksFragmentViewModel

    val webView: WebView? get() = delegate.webView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        location = requireNotNull(arguments?.getString("location")) { "A location argument must be provided" }
        delegate = TurbolinksFragmentDelegate(this, this).apply { onCreate(location) }
        sharedViewModel = TurbolinksSharedViewModel.get(requireActivity())
        viewModel = TurbolinksFragmentViewModel.get(location, this)

        observeLiveData()
    }

    override fun onStart() {
        super.onStart()

        val activity = requireNotNull(context as? TurbolinksActivity) {
            "The fragment Activity must implement TurbolinksActivity"
        }
        delegate.onStart(activity)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onSetupToolbar() {
        onProvideToolbar()?.let {
            NavigationUI.setupWithNavController(it, findNavController())
            it.setNavigationOnClickListener {
                delegate.navigateUp()
            }
        }
    }

    internal fun detachWebView(destinationIsFinishing: Boolean, onDetached: () -> Unit = {}) {
        // Clear the current toolbar title to prevent buggy animation
        // effect when transitioning to the next/previous screen.
        onProvideToolbar()?.title = ""
        delegate.detachWebView(destinationIsFinishing, onDetached)
    }

    fun navigate(location: String, action: String = "advance") {
        delegate.navigate(location, action)
    }

    private fun observeLiveData() {
        viewModel.title.observe(this, Observer {
            onProvideToolbar()?.title = it
        })
    }
}
