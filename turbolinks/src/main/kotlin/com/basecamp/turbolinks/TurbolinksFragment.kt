package com.basecamp.turbolinks

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

abstract class TurbolinksFragment : Fragment() {
    lateinit var location: String
    lateinit var router: TurbolinksRouter
    lateinit var session: TurbolinksSession
    lateinit var navigator: TurbolinksNavigator

    lateinit var sharedViewModel: TurbolinksSharedViewModel
    lateinit var pageViewModel: TurbolinksFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireNotNull(context as? TurbolinksActivity) {
            "The fragment Activity must implement TurbolinksActivity"
        }

        location = currentLocation()
        router = activity.onProvideRouter()
        session = activity.onProvideSession(this)
        navigator = TurbolinksNavigator(this, session, router)
        sharedViewModel = TurbolinksSharedViewModel.get(requireActivity())
        pageViewModel = TurbolinksFragmentViewModel.get(location, this)

        observeLiveData()
    }

    abstract fun onProvideToolbar(): Toolbar?

    protected open fun initToolbar() {
        onProvideToolbar()?.let {
            NavigationUI.setupWithNavController(it, findNavController())
            it.setNavigationOnClickListener { navigateUp() }
        }
    }

    // ----------------------------------------------------------------------------
    // Navigation
    // ----------------------------------------------------------------------------

    fun navigate(location: String, action: String = "advance"): Boolean {
        return navigator.navigate(location, action)
    }

    fun navigateUp(): Boolean {
        return navigator.navigateUp()
    }

    fun navigateBack() {
        navigator.navigateBack()
    }

    fun clearBackStack() {
        navigator.clearBackStack()
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun observeLiveData() {
        pageViewModel.title.observe(this, Observer {
            onProvideToolbar()?.title = it
        })
    }

    private fun currentLocation(): String {
        return requireNotNull(arguments?.getString("location")) {
            "A location argument must be provided"
        }
    }
}
