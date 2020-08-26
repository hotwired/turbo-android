package com.basecamp.turbolinks

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TurbolinksGraphDestination(
    val uri: String
)
