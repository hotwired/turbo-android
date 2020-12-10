package dev.hotwire.turbo.nav

/**
 * Annotation to be on any Fragment that will be used as a destination.
 * For example, `@TurboNavGraphDestination(uri = "turbo://fragment/search")`
 *
 * @property uri The URI to be registered with the Android Navigation components nav graph.
 * @constructor Create empty Turbo nav graph destination
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurboNavGraphDestination(
    val uri: String
)
