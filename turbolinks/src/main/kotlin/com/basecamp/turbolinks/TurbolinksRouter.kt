package com.basecamp.turbolinks

import android.net.Uri
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

    abstract fun getFallbackDeepLinkUri(location: String): Uri
}

enum class PresentationContext {
    DEFAULT, MODAL
}

enum class Presentation {
    DEFAULT, PUSH, POP, REPLACE, REPLACE_ALL, REPLACE_ROOT, NONE
}

enum class NavigationMode {
    IN_CONTEXT, TO_MODAL, DISMISS_MODAL
}
