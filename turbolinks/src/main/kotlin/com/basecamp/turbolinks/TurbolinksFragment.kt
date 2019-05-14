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

    lateinit var router: TurbolinksRouter
    lateinit var session: TurbolinksSession

    lateinit var sharedViewModel: TurbolinksSharedViewModel
    lateinit var viewModel: TurbolinksFragmentViewModel

    val webView: WebView? get() = delegate.webView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireNotNull(context as? TurbolinksActivity) {
            "The fragment Activity must implement TurbolinksActivity"
        }

        location = currentLocation()
        router = activity.onProvideRouter()
        session = activity.onProvideSession(this)
        delegate = TurbolinksFragmentDelegate(this, this).apply { onCreate(location) }
        sharedViewModel = TurbolinksSharedViewModel.get(requireActivity())
        viewModel = TurbolinksFragmentViewModel.get(location, this)

        observeLiveData()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onSetupToolbar() {
        onProvideToolbar()?.let {
            NavigationUI.setupWithNavController(it, findNavController())
            it.setNavigationOnClickListener { navigateUp() }
        }
    }

    fun navigate(location: String, action: String = "advance"): Boolean {
        return delegate.navigator.navigate(location, action)
    }

    fun navigateUp(): Boolean {
        return delegate.navigator.navigateUp()
    }

    fun navigateBack() {
        delegate.navigator.navigateBack()
    }

    fun clearBackStack() {
        delegate.navigator.clearBackStack()
    }

    private fun observeLiveData() {
        viewModel.title.observe(this, Observer {
            onProvideToolbar()?.title = it
        })
    }

    private fun currentLocation(): String {
        return requireNotNull(arguments?.getString("location")) {
            "A location argument must be provided"
        }
    }
}
