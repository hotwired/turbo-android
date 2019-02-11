package com.basecamp.turbolinks

import android.content.Context
import android.webkit.WebView
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = TestBuildConfig::class)

class TurbolinksViewTest {
    @Mock private lateinit var webView: WebView
    private lateinit var tlView: TurbolinksView

    private val context: Context = RuntimeEnvironment.application.applicationContext

    @Before fun setup() {
        MockitoAnnotations.initMocks(this)
        tlView = TurbolinksView(context)
    }

    @Test fun refreshLayoutIsFirstChild() {
        tlView.refreshLayout // Accessing the property instantiates the lazy val

        assertThat(tlView.getChildAt(0) is TurbolinksView.TurbolinksSwipeRefreshLayout).isTrue()
    }

    @Test fun webviewAttachedToRefreshLayout() {
        tlView.attachWebView(webView)

        assertThat(tlView.refreshLayout.getChildAt(1)).isEqualTo(webView) // Child at 0 is CircleImageView
    }
}
