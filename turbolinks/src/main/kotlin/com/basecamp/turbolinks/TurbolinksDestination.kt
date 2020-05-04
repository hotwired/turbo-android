package com.basecamp.turbolinks

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

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

    fun onBeforeNavigation()

    fun controllerForNavigation(location: String?): NavController {
        return fragment.findNavController()
    }

    fun getFallbackDeepLinkUri(location: String): Uri? {
        return null
    }

    fun shouldNavigateTo(newLocation: String): Boolean {
        return true
    }

    fun navigate(
        location: String,
        options: VisitOptions = VisitOptions(),
        bundle: Bundle? = null,
        extras: FragmentNavigator.Extras? = null
    ) {
        navigator.navigate(location, options, bundle, extras)
    }

    fun getNavigationOptions(
        newLocation: String,
        newPathProperties: PathProperties
    ): NavOptions {
        return navOptions {
            anim {
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
            }
        }
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
