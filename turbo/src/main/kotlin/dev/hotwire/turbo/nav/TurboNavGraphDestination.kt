package dev.hotwire.turbo.nav

/**
 * Annotation for each Fragment or Activity that will be registered
 * as a navigation destination.
 *
 * For example:
 *  `@TurboNavGraphDestination(uri = "turbo://fragment/search")`
 *  `class SearchFragment : TurboWebFragment()`
 *
 * @property uri The URI to be registered with the Android Navigation component nav graph.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurboNavGraphDestination(
    val uri: String
)
