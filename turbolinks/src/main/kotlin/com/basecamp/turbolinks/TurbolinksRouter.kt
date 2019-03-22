package com.basecamp.turbolinks

abstract class TurbolinksRouter {
    abstract fun getNavigationAction(location: String, properties: PathProperties): Int?
    abstract fun getModalContextStartAction(location: String): Int
    abstract fun getModalContextDismissAction(location: String): Int
}

enum class PresentationContext {
    DEFAULT, MODAL
}

enum class Presentation {
    PUSH, POP, REPLACE, REPLACE_ALL, NONE
}
