package com.basecamp.turbolinks

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

class TurbolinksFragmentDelegate(private val destination: TurbolinksDestination) {
    internal var fragment = destination.fragment
    internal var location = currentLocation()
    internal var visitOptions = currentVisitOptions()
    internal var sessionName = currentSessionName()
    internal var sessionViewModel = TurbolinksSessionViewModel.get(sessionName, fragment.requireActivity())
    internal var pageViewModel = TurbolinksFragmentViewModel.get(location, fragment)
    internal var navigatedFromModalResult: Boolean = false

    internal lateinit var pathProperties: PathProperties
    internal lateinit var router: TurbolinksRouter
    internal lateinit var session: TurbolinksSession
    internal lateinit var navigator: TurbolinksNavigator

    fun onActivityCreated() {
        val activity = requireNotNull(fragment.context as? TurbolinksActivity) {
            "The fragment Activity must implement TurbolinksActivity"
        }

        router = activity.delegate.router
        session = activity.delegate.getSession(sessionName)
        pathProperties = session.pathConfiguration.properties(location)
        navigator = TurbolinksNavigator(destination)

        initToolbar()
        logEvent("fragment.onActivityCreated", "location" to location)
    }

    fun onStart() {
        logEvent("fragment.onStart", "location" to location)

        navigatedFromModalResult = sessionViewModel.modalResult?.let {
            logEvent("navigateFromModalResult", "location" to it.location, "options" to it.options)
            navigator.navigate(it.location, it.options)
        } ?: false
    }

    fun onDialogDismiss() {
        logEvent("fragment.onDialogDismiss", "location" to location)
        sessionViewModel.sendDialogResult()
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun initToolbar() {
        destination.toolbarForNavigation()?.let {
            NavigationUI.setupWithNavController(it, fragment.findNavController())
            it.setNavigationOnClickListener { navigator.navigateUp() }
        }
    }

    private fun currentLocation(): String {
        return requireNotNull(fragment.arguments?.getString("location")) {
            "A location argument must be provided"
        }
    }

    private fun currentVisitOptions(): VisitOptions {
        val options = VisitOptions.fromJSON(fragment.arguments?.getString("visitOptions"))
        return options ?: VisitOptions()
    }

    private fun currentSessionName(): String {
        return requireNotNull(fragment.arguments?.getString("sessionName")) {
            "A sessionName argument must be provided"
        }
    }

    private fun logEvent(event: String, vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to session.sessionName)
            add("fragment" to fragment.javaClass.simpleName)
        }
        logEvent(event, attributes)
    }
}
