package dev.hotwire.turbo.delegates

import android.app.Activity
import android.os.Build
import com.nhaarman.mockito_kotlin.whenever
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.util.TurboFileProvider
import dev.hotwire.turbo.util.toJson
import dev.hotwire.turbo.views.TurboWebView
import dev.hotwire.turbo.visit.TurboVisit
import dev.hotwire.turbo.visit.TurboVisitOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboFileUploadDelegateTest {
    @Mock
    private lateinit var webView: TurboWebView
    private lateinit var activity: Activity
    private lateinit var session: TurboSession

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        activity = buildActivity(TurboTestActivity::class.java).get()
        session = TurboSession("test", activity, webView)
        session.fileUploadDelegate.coroutineDispatcher = Dispatchers.Main
    }

    @Test
    fun fileProviderDirectoryIsCleared() {
        val dir = TurboFileProvider.directory(activity)

        File(dir, "testFile.txt").apply {
            writeText("text")
        }

        assertThat(dir.listFiles()?.size).isEqualTo(1)
        assertThat(dir.listFiles()?.get(0)?.name).isEqualTo("testFile.txt")

        runBlocking {
            session.fileUploadDelegate.deleteCachedFiles()
            assertThat(dir.listFiles()?.size).isEqualTo(0)
        }
    }
}

internal class TurboTestActivity : Activity()
