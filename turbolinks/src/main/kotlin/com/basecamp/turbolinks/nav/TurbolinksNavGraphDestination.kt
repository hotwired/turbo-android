package com.basecamp.turbolinks.nav

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurbolinksNavGraphDestination(
    val uri: String
)
