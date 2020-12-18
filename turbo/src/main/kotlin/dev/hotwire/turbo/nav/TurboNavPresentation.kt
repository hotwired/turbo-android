package dev.hotwire.turbo.nav

/**
 * Represents how a given navigation destination should be presented and how the app's
 * backstack will be affected.
 */
enum class TurboNavPresentation {
    /**
     * A generic default value when no specific presentation value is provided and results in
     * generally accepted "normal" behavior — replacing the root when on the start destination and
     * going to the start destination again, popping when the location is in the immediate
     * backstack, replacing when going to the same destination, and pushing in all other cases.
     */
    DEFAULT,

    /**
     * Pushes a new location onto the nav stack.
     */
    PUSH,

    /**
     * Pops a location off the nav stack.
     */
    POP,

    /**
     * Pops the current location off the nav stack and pushes a new location onto the nav stack.
     */
    REPLACE,

    /**
     * Clears the entire back stack and retains the start destination for the nav host.
     */
    CLEAR_ALL,

    /**
     * Clears the entire back stack and replaces the start destination for the nav host.
     */
    REPLACE_ROOT,

    /**
     * Will force a reload of the current location and clear out any saved visit options.
     */
    REFRESH,

    /**
     * Will result in no navigation action being taken.
     */
    NONE
}
