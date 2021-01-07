package dev.hotwire.turbo.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class TurboDispatcherProvider(
    val main: CoroutineDispatcher,
    var io: CoroutineDispatcher
)

val dispatcherProvider = TurboDispatcherProvider(
    main = Dispatchers.Main,
    io = Dispatchers.IO
)
