package dev.hotwire.turbo.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal data class TurboDispatcherProvider(
    val main: CoroutineDispatcher,
    var io: CoroutineDispatcher
)

internal val dispatcherProvider = TurboDispatcherProvider(
    main = Dispatchers.Main,
    io = Dispatchers.IO
)
