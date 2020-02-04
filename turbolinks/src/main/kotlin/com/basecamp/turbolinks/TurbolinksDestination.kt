package com.basecamp.turbolinks

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

interface TurbolinksDestination {
    val fragment: Fragment
        get() = this as Fragment

    val navHost: TurbolinksNavHost
        get() = fragment.parentFragment as TurbolinksNavHost

    val location: String
        get() = delegate().location

    val visitOptions: VisitOptions
        get() = delegate().visitOptions

    val pathProperties: PathProperties
        get() = delegate().pathProperties

    val sessionName: String
        get() = delegate().sessionName

    val router: TurbolinksRouter
        get() = delegate().router

    val session: TurbolinksSession
        get() = navHost.session

    val webView: TurbolinksWebView
        get() = session.webView

    val sessionViewModel: TurbolinksSessionViewModel
        get() = delegate().sessionViewModel

    val pageViewModel: TurbolinksFragmentViewModel
        get() = delegate().pageViewModel

    val navigator: TurbolinksNavigator
        get() = delegate().navigator

    fun delegate(): TurbolinksFragmentDelegate

    fun toolbarForNavigation(): Toolbar?

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
}
