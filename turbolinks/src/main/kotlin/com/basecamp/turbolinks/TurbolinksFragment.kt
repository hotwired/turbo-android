package com.basecamp.turbolinks

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

abstract class TurbolinksFragment : Fragment() {
    lateinit var location: String
    lateinit var visitOptions: VisitOptions
    lateinit var pathProperties: PathProperties
    lateinit var sessionName: String
    lateinit var router: TurbolinksRouter
    lateinit var session: TurbolinksSession
    lateinit var navigator: TurbolinksNavigator

    lateinit var sharedViewModel: TurbolinksSharedViewModel
    lateinit var pageViewModel: TurbolinksFragmentViewModel

    internal var navigatedFromModalResult: Boolean = false

    abstract fun toolbarForNavigation(): Toolbar?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        location = currentLocation()
        visitOptions = currentVisitOptions()
        sessionName = currentSessionName()
        sharedViewModel = TurbolinksSharedViewModel.get(requireActivity())
        pageViewModel = TurbolinksFragmentViewModel.get(location, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireNotNull(context as? TurbolinksActivity) {
            "The fragment Activity must implement TurbolinksActivity"
        }

        router = activity.delegate.router
        session = activity.delegate.getSession(sessionName)
        pathProperties = session.pathConfiguration.properties(location)
        navigator = TurbolinksNavigator(this, session, router)

        initToolbar()
        logEvent("fragment.onActivityCreated", "location" to location)
    }

    override fun onStart() {
        super.onStart()
        logEvent("fragment.onStart", "location" to location)

        navigatedFromModalResult = sharedViewModel.modalResult?.let {
            logEvent("navigateFromModalResult", "location" to it.location, "options" to it.options)
            navigator.navigate(it.location, it.options)
        } ?: false
    }

    // ----------------------------------------------------------------------------
    // Navigation
    // ----------------------------------------------------------------------------

    fun navigate(location: String,
                 options: VisitOptions = VisitOptions(),
                 bundle: Bundle? = null): Boolean {
        return navigator.navigate(location, options, null, bundle)
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

    private fun initToolbar() {
        toolbarForNavigation()?.let {
            NavigationUI.setupWithNavController(it, findNavController())
            it.setNavigationOnClickListener { navigateUp() }
        }
    }

    private fun currentLocation(): String {
        return requireNotNull(arguments?.getString("location")) {
            "A location argument must be provided"
        }
    }

    private fun currentVisitOptions(): VisitOptions {
        val options = VisitOptions.fromJSON(arguments?.getString("visitOptions"))
        return options ?: VisitOptions()
    }

    private fun currentSessionName(): String {
        return requireNotNull(arguments?.getString("sessionName")) {
            "A sessionName argument must be provided"
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to session.sessionName)
            add("fragment" to this@TurbolinksFragment.javaClass.simpleName)
        }
        logEvent(event, attributes)
    }
}
