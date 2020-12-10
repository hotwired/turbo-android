package dev.hotwire.turbo.nav

/**
 * Describes the UI context (eg, modal or normal) within which the [TurboNavPresentation] is
 * executed.
 *
 * @constructor Create empty Turbo nav presentation context
 */
enum class TurboNavPresentationContext {
    /**
     * A standard context where navigation is pushed or popped to/from the stack. Typically these
     * views are the full size of the app, and have a back button.
     *
     * @constructor Create empty D e f a u l t
     */
    DEFAULT,

    /**
     * Modals are dismissable and are temporary parts of the app's navigation flow. They
     * are displayed for a single specific purpose, but when dismissed the app returns to the main
     * [DEFAULT] context.
     *
     * @constructor Create empty M o d a l
     */
    MODAL
}
