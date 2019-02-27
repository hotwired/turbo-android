package com.basecamp.turbolinks

abstract class TurbolinksRouter {
    abstract fun getNavigationAction(location: String): Int?
}
