package dev.hotwire.turbo

import dev.hotwire.turbo.util.dispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
open class BaseRepositoryTest : BaseUnitTest() {
    private val server = MockWebServer()
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    override fun setup() {
        super.setup()
        Dispatchers.setMain(testDispatcher)
        dispatcherProvider.io = Dispatchers.Main
        server.start()
    }

    override fun teardown() {
        super.teardown()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        server.shutdown()
    }

    protected fun client(): OkHttpClient {
        return OkHttpClient.Builder()
                .dispatcher(Dispatcher(SynchronousExecutorService()))
                .build()
    }

    protected fun baseUrl(): String {
        return server.url("/").toString()
    }

    protected fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = loadAsset(fileName)
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse().apply {
            headers.forEach { addHeader(it.key, it.value) }
            setBody(source.readString(StandardCharsets.UTF_8))
        }

        server.enqueue(mockResponse)
    }

    private fun loadAsset(fileName: String): InputStream {
        return javaClass.classLoader?.getResourceAsStream("http-responses/$fileName")
                ?: throw IllegalStateException("Couldn't load api response file")
    }

    private class SynchronousExecutorService : ExecutorService {
        override fun isShutdown(): Boolean = false
        override fun isTerminated(): Boolean = false

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = false
        override fun execute(command: Runnable) = command.run()

        override fun shutdown() {}
        override fun shutdownNow(): List<Runnable>? = null

        override fun <T> submit(task: Callable<T>): Future<T>? = null
        override fun <T> submit(task: Runnable, result: T): Future<T>? = null
        override fun submit(task: Runnable): Future<*>? = null

        override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>? = null
        override fun <T> invokeAny(tasks: Collection<Callable<T>>): T? = null
        override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T? = null
        override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>? = null
    }
}
