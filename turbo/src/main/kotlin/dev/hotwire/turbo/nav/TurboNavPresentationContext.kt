package dev.hotwire.turbo.nav

/**
 * Describes the UI context (eg, modal or normal) within which the [TurboNavPresentation] is
 * executed.
 */
enum class TurboNavPresentationContext {
    /**
     * A standard context where navigation is pushed or popped to/from the stack. Typically these
     * views are the full size of the app, and have a back button.
     */
    DEFAULT,

    /**
     * Modals are dismissable and are temporary parts of the app's navigation flow. They are
     * not kept on the backstack once they are navigated away from. They are displayed for a
     * single specific purpose (like to present a form), but when dismissed the app returns
     * to the main [DEFAULT] context.
     */
    MODAL
}
