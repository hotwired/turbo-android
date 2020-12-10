package dev.hotwire.turbo.session

import android.app.Activity
import android.os.Build
import dev.hotwire.turbo.util.toJson
import dev.hotwire.turbo.views.TurbolinksWebView
import dev.hotwire.turbo.visit.TurbolinksVisit
import dev.hotwire.turbo.visit.TurbolinksVisitOptions
import com.nhaarman.mockito_kotlin.whenever
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksSessionTest {
    @Mock
    private lateinit var callback: TurbolinksSessionCallback
    @Mock
    private lateinit var webView: TurbolinksWebView
    private lateinit var activity: Activity
    private lateinit var session: TurbolinksSession
    private lateinit var visit: TurbolinksVisit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        activity = buildActivity(TurbolinksTestActivity::class.java).get()
        session = TurbolinksSession.getNew("test", activity, webView)
        visit = TurbolinksVisit(
            location = "https://basecamp.com",
            destinationIdentifier = 1,
            restoreWithCachedSnapshot = false,
            reload = false,
            callback = callback,
            identifier = "",
            options = TurbolinksVisitOptions()
        )

        whenever(callback.isActive()).thenReturn(true)
    }

    @Test
    fun getNewIsAlwaysNewInstance() {
        val session = TurbolinksSession.getNew("test", activity, webView)
        val newSession = TurbolinksSession.getNew("test", activity, webView)

        assertThat(session).isNotEqualTo(newSession)
    }

    @Test
    fun visitProposedToLocationFiresCallback() {
        val options = TurbolinksVisitOptions()

        session.currentVisit = visit
        session.visitProposedToLocation(visit.location, options.toJson())

        verify(callback).visitProposedToLocation(visit.location, options)
    }

    @Test
    fun visitStartedSavesCurrentVisitIdentifier() {
        val visitIdentifier = "12345"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.visitStarted(visitIdentifier, true, "https://basecamp.com")

        assertThat(session.currentVisit?.identifier).isEqualTo(visitIdentifier)
    }

    @Test
    fun visitRequestFailedWithStatusCodeCallsAdapter() {
        val visitIdentifier = "12345"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.visitRequestFailedWithStatusCode(visitIdentifier, true, 500)

        verify(callback).requestFailedWithStatusCode(true, 500)
    }

    @Test
    fun visitCompletedCallsAdapter() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.visitCompleted(visitIdentifier, restorationIdentifier)

        verify(callback).visitCompleted(false)
    }

    @Test
    fun visitCompletedSavesRestorationIdentifier() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"
        assertThat(session.restorationIdentifiers.size()).isEqualTo(0)

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.visitCompleted(visitIdentifier, restorationIdentifier)

        assertThat(session.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test
    fun pageLoadedSavesRestorationIdentifier() {
        val restorationIdentifier = "67890"
        assertThat(session.restorationIdentifiers.size()).isEqualTo(0)

        session.currentVisit = visit
        session.pageLoaded(restorationIdentifier)

        assertThat(session.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test
    fun pendingVisitIsVisitedWhenReady() {
        session.currentVisit = visit
        session.visitPending = true

        session.turbolinksIsReady(true)
        assertThat(session.visitPending).isFalse()
    }

    @Test
    fun resetToColdBoot() {
        session.currentVisit = visit
        session.isReady = true
        session.isColdBooting = false
        session.reset()

        assertThat(session.isReady).isFalse()
        assertThat(session.isColdBooting).isFalse()
    }

    @Test
    fun resetToColdBootClearsIdentifiers() {
        val visitIdentifier = "12345"
        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.coldBootVisitIdentifier = "0"
        session.reset()

        assertThat(session.coldBootVisitIdentifier).isEmpty()
        assertThat(session.currentVisit?.identifier).isEmpty()
    }

    @Test
    fun webViewIsNotNull() {
        assertThat(session.webView).isNotNull
    }
}

internal class TurbolinksTestActivity : Activity()
