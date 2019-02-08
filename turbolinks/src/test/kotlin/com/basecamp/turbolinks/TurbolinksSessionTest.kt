package com.basecamp.turbolinks

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = TestBuildConfig::class)

class TurbolinksSessionTest {
    @Mock private lateinit var callback: TurbolinksCallback
    private lateinit var activity: Activity
    private lateinit var tlView: TurbolinksView
    private lateinit var tlSession: TurbolinksSession

    private val context: Context = RuntimeEnvironment.application.applicationContext

    @Before fun setup() {
        MockitoAnnotations.initMocks(this)

        tlView = TurbolinksView(context)
        tlSession = TurbolinksSession.getNew(context)
        activity = Robolectric.setupActivity(TurbolinksTestActivity::class.java)
    }

    @Test fun getNewIsAlwaysNewInstance() {
        val session = TurbolinksSession.getNew(context)

        assertThat(session).isNotEqualTo(TurbolinksSession.getNew(context))
    }

    @Test fun visitProposedToLocationWithActionFiresCallback() {
        tlSession.activity(activity)
                .callback(callback)
        tlSession.visitProposedToLocationWithAction("https://basecamp.com", TurbolinksSession.ACTION_ADVANCE)

        verify(callback).visitProposedToLocationWithAction("https://basecamp.com", TurbolinksSession.ACTION_ADVANCE)
    }

    @Test
    fun visitStartedSavesCurrentVisitIdentifier() {
        val visitIdentifier = "12345"

        tlSession.currentVisitIdentifier = ""

        assertThat(tlSession.currentVisitIdentifier).isNotEqualTo(visitIdentifier)

        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.visitStarted(visitIdentifier, true, "https://basecamp.com", visitIdentifier)

        assertThat(tlSession.currentVisitIdentifier).isEqualTo(visitIdentifier)
    }

    @Test fun visitRequestFailedWithStatusCodeCallsAdapter() {
        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.currentVisitIdentifier = ""
        tlSession.visitRequestFailedWithStatusCode("", 500)

        verify(callback).requestFailedWithStatusCode(500)
    }

    @Test fun visitCompletedCallsAdapter() {
        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.currentVisitIdentifier = "0"
        tlSession.visitCompleted("0")

        verify(callback).visitCompleted()
    }

    @Test fun visitStartedSavesRestorationIdentifier() {
        assertThat(tlSession.restorationIdentifiers.size()).isEqualTo(0)

        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.visitStarted("0", false, "https://basecamp.com", "0")

        assertThat(tlSession.restorationIdentifiers.size()).isEqualTo(1)
    }

    @Test fun hideProgressViewNullsView() {
        tlSession.activity(activity)
        tlSession.isReady = true
        tlSession.view(tlView)
        tlSession.progressView = FrameLayout(context)
        tlSession.currentVisitIdentifier = "0"
        tlSession.removeTransitionalViews("0")

        assertThat(tlSession.progressView!!.parent).isNull()
    }

    @Test(expected = IllegalArgumentException::class) fun addJavascriptInterfaceWithReservedName() {
        tlSession.addJavascriptInterface(Any(), "TurbolinksSession")
    }

    @Test fun addJavascriptInterfaceAddsToMap() {
        assertThat(tlSession.javascriptInterfaces.size).isEqualTo(0)
        tlSession.addJavascriptInterface(Any(), "TestJavascriptInterface")
        assertThat(tlSession.javascriptInterfaces.size).isEqualTo(1)
    }

    @Test fun resetToColdBoot() {
        tlSession.activity(activity)
                .callback(callback)
        tlSession.isLoadingBridge = true
        tlSession.isReady = true
        tlSession.isColdBooting = false
        tlSession.reset()

        assertThat(tlSession.isLoadingBridge).isFalse()
        assertThat(tlSession.isReady).isFalse()
        assertThat(tlSession.isColdBooting).isFalse()
    }

    @Test fun resetToColdBootClearsIdentifiers() {
        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.coldBootVisitIdentifier = "0"
        tlSession.currentVisitIdentifier = "1"
        tlSession.reset()

        assertThat(tlSession.coldBootVisitIdentifier).isEmpty()
        assertThat(tlSession.currentVisitIdentifier).isEmpty()
    }

    @Test fun resetToColdBootClearsVisits() {
        val visitIdentifier = "12345"

        tlSession.activity(activity)
                .callback(callback)
                .view(tlView)
        tlSession.visitStarted(visitIdentifier, true, "https://basecamp.com", visitIdentifier)

        assertThat(tlSession.currentVisitIdentifier).isEqualTo(visitIdentifier)
        assertThat(tlSession.visits.size).isEqualTo(1)
        tlSession.reset()
        assertThat(tlSession.visits.size).isEqualTo(0)
    }

    @Test fun webViewIsNotNull() {
        assertThat(tlSession.webView).isNotNull()
    }

    @Test fun webViewHasCorrectSettings() {
        with(tlSession.webView.settings) {
            assertThat(javaScriptEnabled).isTrue()
            assertThat(domStorageEnabled).isTrue()
            assertThat(databaseEnabled).isTrue()
        }
    }

    @Test fun encodeUrlProperlyEncodes() {
        val url = "http://basecamp.com/search?q=test test + testing & /testfile .mp4"
        assertThat(url.urlEncode()).doesNotContain(" ")
    }
}

internal class TurbolinksTestActivity : Activity()
