package com.basecamp.turbolinks

abstract class TurbolinksRouter {
    abstract fun getNavigationAction(location: String): Int?
    abstract fun getPresentationContext(location: String): PresentationContext
}

enum class PresentationContext {
    DEFAULT, MODAL
}

enum class Presentation {
    PUSH, POP, REPLACE, REPLACE_ALL, NONE
}
