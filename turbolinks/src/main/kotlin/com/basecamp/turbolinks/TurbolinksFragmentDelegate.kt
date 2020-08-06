package com.basecamp.turbolinks

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

class TurbolinksFragmentDelegate(private val destination: TurbolinksDestination) {
    internal val fragment = destination.fragment
    internal val location = currentLocation()
    internal val previousLocation = previousLocation()
    internal val sessionName = currentSessionName()
    internal val sessionViewModel = TurbolinksSessionViewModel.get(sessionName, fragment.requireActivity())
    internal val pageViewModel = TurbolinksFragmentViewModel.get(location, fragment)

    internal lateinit var pathConfiguration: PathConfiguration
    internal lateinit var pathProperties: PathProperties
    internal lateinit var session: TurbolinksSession
    internal lateinit var navigator: TurbolinksNavigator

    fun onActivityCreated() {
        session = destination.session
        pathConfiguration = session.pathConfiguration
        pathProperties = pathConfiguration.properties(location)
        navigator = TurbolinksNavigator(destination)

        initToolbar()
        logEvent("fragment.onActivityCreated", "location" to location)
    }

    fun onStart() {
        logEvent("fragment.onStart", "location" to location)
    }

    fun onStop() {
        logEvent("fragment.onStop", "location" to location)
    }

    fun onStartAfterDialogCancel() {
        logEvent("fragment.onStartAfterDialogCancel", "location" to location)
    }

    fun onStartAfterModalResult(result: TurbolinksModalResult) {
        logEvent("fragment.onStartAfterModalResult", "location" to result.location, "options" to result.options)
        if (result.shouldNavigate) {
            navigator.navigate(result.location, result.options, result.bundle)
        }
    }

    fun onDialogCancel() {
        logEvent("fragment.onDialogCancel", "location" to location)
        if (!sessionViewModel.modalResultExists) {
            sessionViewModel.sendDialogResult()
        }
    }

    // ----------------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------------

    private fun initToolbar() {
        destination.toolbarForNavigation()?.let {
            NavigationUI.setupWithNavController(it, fragment.findNavController())
            it.setNavigationOnClickListener { destination.navigateUp() }
        }
    }

    private fun currentLocation(): String {
        return requireNotNull(fragment.arguments?.getString("location")) {
            "A location argument must be provided"
        }
    }

    private fun previousLocation(): String? {
        return fragment.arguments?.getString("previousLocation")
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
