package com.basecamp.turbolinks

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface TurbolinksDestination {
    val fragment: Fragment
        get() = this as Fragment

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
        get() = delegate().session

    val webView: TurbolinksWebView
        get() = session.webView

    val sharedViewModel: TurbolinksSharedViewModel
        get() = delegate().sharedViewModel

    val pageViewModel: TurbolinksFragmentViewModel
        get() = delegate().pageViewModel

    val navigatedFromModalResult: Boolean
        get() = delegate().navigatedFromModalResult

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
