package com.basecamp.turbolinks.util

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurbolinksNavGraphDestination(
    val uri: String
)
