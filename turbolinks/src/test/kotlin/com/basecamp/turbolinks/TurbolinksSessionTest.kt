package com.basecamp.turbolinks

import android.app.Activity
import android.os.Build
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksSessionTest {
    @Mock private lateinit var callback: TurbolinksSessionCallback
    private lateinit var activity: Activity
    private lateinit var session: TurbolinksSession
    private lateinit var visit: TurbolinksVisit

    @Before fun setup() {
        MockitoAnnotations.initMocks(this)

        activity = Robolectric.setupActivity(TurbolinksTestActivity::class.java)
        session = TurbolinksSession.getNew("test", activity)
        visit = TurbolinksVisit(
                location = "https://basecamp.com",
                destinationIdentifier = 1,
                restoreWithCachedSnapshot = false,
                reload = false,
                callback = callback,
                identifier = ""
        )
    }

    @Test fun getNewIsAlwaysNewInstance() {
        val session = TurbolinksSession.getNew("test", activity)

        assertThat(session).isNotEqualTo(TurbolinksSession.getNew("test", activity))
    }

    @Test fun visitProposedToLocationWithActionFiresCallback() {
        session.currentVisit = visit
        session.visitProposedToLocationWithAction(visit.location, TurbolinksSession.ACTION_ADVANCE)

        verify(callback).visitProposedToLocation(
                visit.location,
                TurbolinksSession.ACTION_ADVANCE,
                PathProperties())
    }

    @Test
    fun visitStartedSavesCurrentVisitIdentifier() {
        val visitIdentifier = "12345"
        session.currentVisit = visit
        session.visitStarted(visitIdentifier, true, "https://basecamp.com", visitIdentifier)

        assertThat(session.currentVisit.identifier).isEqualTo(visitIdentifier)
    }

    @Test fun visitRequestFailedWithStatusCodeCallsAdapter() {
        session.currentVisit = visit
        session.visitRequestFailedWithStatusCode("", 500)

        verify(callback).requestFailedWithStatusCode(500)
    }

    @Test fun visitCompletedCallsAdapter() {
        session.currentVisit = visit.copy(identifier = "0")
        session.visitCompleted("0")

        verify(callback).visitCompleted()
    }

    @Test fun visitStartedSavesRestorationIdentifier() {
        assertThat(session.restorationIdentifiers.size()).isEqualTo(0)

        session.currentVisit = visit
        session.visitStarted("0", false, "https://basecamp.com", "0")

        assertThat(session.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test fun pendingVisitIsVisitedWhenReady() {
        session.currentVisit = visit
        session.visitPending = true

        session.turbolinksIsReady(true)
        assertThat(session.visitPending).isFalse()
    }

    @Test fun resetToColdBoot() {
        session.currentVisit = visit
        session.isReady = true
        session.isColdBooting = false
        session.reset()

        assertThat(session.isReady).isFalse()
        assertThat(session.isColdBooting).isFalse()
    }

    @Test fun resetToColdBootClearsIdentifiers() {
        session.currentVisit = visit.copy(identifier = "1")
        session.coldBootVisitIdentifier = "0"
        session.reset()

        assertThat(session.coldBootVisitIdentifier).isEmpty()
        assertThat(session.currentVisit.identifier).isEmpty()
    }

    @Test fun webViewIsNotNull() {
        assertThat(session.webView).isNotNull
    }

    @Test fun webViewHasCorrectSettings() {
        with(session.webView.settings) {
            assertThat(javaScriptEnabled).isTrue()
            assertThat(domStorageEnabled).isTrue()
        }
    }

    @Test fun encodeUrlProperlyEncodes() {
        val url = "http://basecamp.com/search?q=test test + testing & /testfile .mp4"
        assertThat(url.urlEncode()).doesNotContain(" ")
    }
}

internal class TurbolinksTestActivity : Activity()
