package dev.hotwire.turbo.nav

/**
 * Represents how a given navigation destination should be presented when the current
 * location path on the backstack matches the new location path *and* a query string is
 * present in either location.
 *
 * Example situation:
 *  current location: /feature
 *  new location:     /feature?filter=true
 *
 * Another example situation:
 *  current location: /feature?filter=a
 *  new location:     /feature?filter=b
 */
enum class TurboNavQueryStringPresentation {
    /**
     * A generic default value when no specific presentation value is provided and results in
     * generally accepted "normal" behavior — replacing the root when on the start destination and
     * going to the start destination again, popping when the location is in the immediate
     * backstack, replacing when going to the same destination, and pushing in all other cases.
     */
    DEFAULT,

    /**
     * Pops the current location off the nav stack and pushes the new location onto the nav stack.
     * If you use query strings in your app to act as a way to filter results in a destination,
     * this allows you to present the new (filtered) destination without adding onto the backstack.
     */
    REPLACE
}
