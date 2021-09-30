package dev.hotwire.turbo.session

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.whenever
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.util.toJson
import dev.hotwire.turbo.views.TurboWebView
import dev.hotwire.turbo.visit.TurboVisit
import dev.hotwire.turbo.visit.TurboVisitOptions
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
class TurboSessionTest {
    @Mock
    private lateinit var callback: TurboSessionCallback
    @Mock
    private lateinit var webView: TurboWebView
    @Mock
    private lateinit var navDestination: TurboNavDestination
    private lateinit var activity: AppCompatActivity
    private lateinit var session: TurboSession
    private lateinit var visit: TurboVisit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        activity = buildActivity(TurboTestActivity::class.java).get()
        session = TurboSession("test", activity, webView)
        visit = TurboVisit(
            location = "https://turbo.hotwired.dev",
            destinationIdentifier = 1,
            restoreWithCachedSnapshot = false,
            reload = false,
            callback = callback,
            identifier = "",
            options = TurboVisitOptions()
        )

        whenever(callback.visitNavDestination()).thenReturn(navDestination)
        whenever(navDestination.isActive).thenReturn(true)
    }

    @Test
    fun getNewIsAlwaysNewInstance() {
        val session = TurboSession("test", activity, webView)
        val newSession = TurboSession("test", activity, webView)

        assertThat(session).isNotEqualTo(newSession)
    }

    @Test
    fun visitProposedToLocationFiresCallback() {
        val options = TurboVisitOptions()

        session.currentVisit = visit
        session.visitProposedToLocation(visit.location, options.toJson())

        verify(callback).visitProposedToLocation(visit.location, options)
    }

    @Test
    fun visitStartedSavesCurrentVisitIdentifier() {
        val visitIdentifier = "12345"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.visitStarted(visitIdentifier, true, "https://turbo.hotwired.dev")

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
    fun visitFormSubmissionStartedFiresCallback() {
        session.currentVisit = visit
        session.formSubmissionStarted(visit.location)

        verify(callback).formSubmissionStarted(visit.location)
    }

    @Test
    fun visitFormSubmissionFinishedFiresCallback() {
        session.currentVisit = visit
        session.formSubmissionFinished(visit.location)

        verify(callback).formSubmissionFinished(visit.location)
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

        session.turboIsReady(true)
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
    fun restoreCurrentVisit() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.turboIsReady(true)
        session.pageLoaded(restorationIdentifier)

        assertThat(session.restoreCurrentVisit(callback)).isTrue()
        verify(callback, times(2)).visitCompleted(false)
    }

    @Test
    fun restoreCurrentVisitFailsWithNoRestorationIdentifier() {
        val visitIdentifier = "12345"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.turboIsReady(true)

        assertThat(session.restoreCurrentVisit(callback)).isFalse()
        verify(callback, times(1)).visitCompleted(false)
    }

    @Test
    fun restoreCurrentVisitFailsWithSessionNotReady() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"

        session.currentVisit = visit.copy(identifier = visitIdentifier)
        session.pageLoaded(restorationIdentifier)
        session.turboIsReady(false)

        assertThat(session.restoreCurrentVisit(callback)).isFalse()
        verify(callback, never()).visitCompleted(false)
    }

    @Test
    fun webViewIsNotNull() {
        assertThat(session.webView).isNotNull
    }
}

internal class TurboTestActivity : AppCompatActivity()
