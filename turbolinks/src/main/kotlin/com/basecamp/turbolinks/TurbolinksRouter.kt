package com.basecamp.turbolinks

import androidx.navigation.NavOptions

abstract class TurbolinksRouter {
    abstract fun shouldNavigate(currentLocation: String,
                                newLocation: String,
                                currentPathProperties: PathProperties,
                                newPathProperties: PathProperties): Boolean

    abstract fun getNavigationOptions(currentLocation: String,
                                      newLocation: String,
                                      currentPathProperties: PathProperties,
                                      newPathProperties: PathProperties): NavOptions?
}

enum class PresentationContext {
    DEFAULT, MODAL
}

enum class Presentation {
    PUSH, POP, REPLACE, REPLACE_ALL, NONE
}
