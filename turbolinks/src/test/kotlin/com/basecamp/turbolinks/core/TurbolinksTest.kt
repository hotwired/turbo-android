package com.basecamp.turbolinks.core

import android.app.Activity
import android.os.Build
import com.basecamp.turbolinks.util.TurbolinksCallback
import com.basecamp.turbolinks.util.toJson
import com.basecamp.turbolinks.views.TurbolinksWebView
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
class TurbolinksTest {
    @Mock
    private lateinit var callback: TurbolinksCallback
    @Mock
    private lateinit var webView: TurbolinksWebView
    private lateinit var activity: Activity
    private lateinit var turbolinks: Turbolinks
    private lateinit var visit: TurbolinksVisit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        activity = buildActivity(TurbolinksTestActivity::class.java).get()
        turbolinks = Turbolinks.getNew("test", activity, webView)
        visit = TurbolinksVisit(
            location = "https://basecamp.com",
            destinationIdentifier = 1,
            restoreWithCachedSnapshot = false,
            reload = false,
            callback = callback,
            identifier = "",
            options = VisitOptions()
        )

        whenever(callback.isActive()).thenReturn(true)
    }

    @Test
    fun getNewIsAlwaysNewInstance() {
        val turbolinks = Turbolinks.getNew("test", activity, webView)
        val newTurbolinks = Turbolinks.getNew("test", activity, webView)

        assertThat(turbolinks).isNotEqualTo(newTurbolinks)
    }

    @Test
    fun visitProposedToLocationFiresCallback() {
        val options = VisitOptions()

        turbolinks.currentVisit = visit
        turbolinks.visitProposedToLocation(visit.location, options.toJson())

        verify(callback).visitProposedToLocation(visit.location, options)
    }

    @Test
    fun visitStartedSavesCurrentVisitIdentifier() {
        val visitIdentifier = "12345"

        turbolinks.currentVisit = visit.copy(identifier = visitIdentifier)
        turbolinks.visitStarted(visitIdentifier, true, "https://basecamp.com")

        assertThat(turbolinks.currentVisit.identifier).isEqualTo(visitIdentifier)
    }

    @Test
    fun visitRequestFailedWithStatusCodeCallsAdapter() {
        val visitIdentifier = "12345"

        turbolinks.currentVisit = visit.copy(identifier = visitIdentifier)
        turbolinks.visitRequestFailedWithStatusCode(visitIdentifier, true, 500)

        verify(callback).requestFailedWithStatusCode(true, 500)
    }

    @Test
    fun visitCompletedCallsAdapter() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"

        turbolinks.currentVisit = visit.copy(identifier = visitIdentifier)
        turbolinks.visitCompleted(visitIdentifier, restorationIdentifier)

        verify(callback).visitCompleted(false)
    }

    @Test
    fun visitCompletedSavesRestorationIdentifier() {
        val visitIdentifier = "12345"
        val restorationIdentifier = "67890"
        assertThat(turbolinks.restorationIdentifiers.size()).isEqualTo(0)

        turbolinks.currentVisit = visit.copy(identifier = visitIdentifier)
        turbolinks.visitCompleted(visitIdentifier, restorationIdentifier)

        assertThat(turbolinks.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test
    fun pageLoadedSavesRestorationIdentifier() {
        val restorationIdentifier = "67890"
        assertThat(turbolinks.restorationIdentifiers.size()).isEqualTo(0)

        turbolinks.currentVisit = visit
        turbolinks.pageLoaded(restorationIdentifier)

        assertThat(turbolinks.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test
    fun pendingVisitIsVisitedWhenReady() {
        turbolinks.currentVisit = visit
        turbolinks.visitPending = true

        turbolinks.turbolinksIsReady(true)
        assertThat(turbolinks.visitPending).isFalse()
    }

    @Test
    fun resetToColdBoot() {
        turbolinks.currentVisit = visit
        turbolinks.isReady = true
        turbolinks.isColdBooting = false
        turbolinks.reset()

        assertThat(turbolinks.isReady).isFalse()
        assertThat(turbolinks.isColdBooting).isFalse()
    }

    @Test
    fun resetToColdBootClearsIdentifiers() {
        val visitIdentifier = "12345"
        turbolinks.currentVisit = visit.copy(identifier = visitIdentifier)
        turbolinks.coldBootVisitIdentifier = "0"
        turbolinks.reset()

        assertThat(turbolinks.coldBootVisitIdentifier).isEmpty()
        assertThat(turbolinks.currentVisit.identifier).isEmpty()
    }

    @Test
    fun webViewIsNotNull() {
        assertThat(turbolinks.webView).isNotNull
    }
}

internal class TurbolinksTestActivity : Activity()
