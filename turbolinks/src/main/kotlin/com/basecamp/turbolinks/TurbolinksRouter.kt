package com.basecamp.turbolinks

import androidx.navigation.NavOptions

abstract class TurbolinksRouter {
    abstract fun shouldNavigate(location: String): Boolean
    abstract fun getNavigationOptions(location: String, pathProperties: PathProperties): NavOptions?
}

enum class PresentationContext {
    DEFAULT, MODAL
}

enum class Presentation {
    PUSH, POP, REPLACE, REPLACE_ALL, NONE
}
