package dev.hotwire.turbo.nav

/**
 * Annotation to be on any Fragment that will be used as a destination.
 * For example, `@TurbolinksNavGraphDestination(uri = "hey://fragment/search")`
 *
 * @property uri The URI to be registered with the Android Navigation components nav graph.
 * @constructor Create empty Turbolinks nav graph destination
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurbolinksNavGraphDestination(
    val uri: String
)
