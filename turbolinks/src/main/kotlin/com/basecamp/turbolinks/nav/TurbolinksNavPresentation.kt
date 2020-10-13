package com.basecamp.turbolinks.nav

/**
 * Represents how a given navigation event should be executed.
 *
 * @constructor Create empty Turbolinks nav presentation
 */
enum class TurbolinksNavPresentation {
    /**
     * A generic default value when no specific presentation value is provided and results in
     * generally accepted "normal" behavior — replacing the root when on the start destination and
     * going to the start destination again, popping when the location is in the back stack,
     * replacing when going to the same destination, and pushing in all other cases.
     *
     * @constructor
     */
    DEFAULT,

    /**
     * Pushes a new location onto the nav stack.
     *
     * @constructor
     */
    PUSH,

    /**
     * Pops a location off the nav stack.
     *
     * @constructor
     */
    POP,

    /**
     * Pops the last location off the nav stack and pushes a new location onto the nav stack.
     *
     * @constructor
     */
    REPLACE,

    /**
     * Clears the entire back stack and retains the start destination for the nav host.
     *
     * @constructor
     */
    REPLACE_ALL,

    /**
     * Pops up to the given destination first (clearing anything that doesn't match), then navigates.
     *
     * @constructor
     */
    REPLACE_ROOT,

    /**
     * Will force a reload of the current location and clear out any saved visit options.
     *
     * @constructor
     */
    REFRESH,

    /**
     * Will result in no navigation action being taken.
     *
     * @constructor
     */
    NONE
}
