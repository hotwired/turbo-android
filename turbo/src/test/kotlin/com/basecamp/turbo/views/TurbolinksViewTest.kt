package com.basecamp.turbo.views

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import com.basecamp.turbo.R
import kotlinx.android.synthetic.main.turbolinks_view.view.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksViewTest {
    @Mock private lateinit var webView: WebView
    private lateinit var view: ViewGroup
    private lateinit var context: Context

    @Before fun setup() {
        MockitoAnnotations.openMocks(this)

        context = ApplicationProvider.getApplicationContext()
        view = LayoutInflater.from(context).inflate(R.layout.turbolinks_default, null) as ViewGroup
    }

    @Test fun refreshLayoutIsFirstChild() {
        assertThat(view.turbolinks_view.getChildAt(0) is TurbolinksSwipeRefreshLayout).isTrue()
    }

    @Test fun webviewAttachedToRefreshLayout() {
        view.turbolinks_view.attachWebView(webView) {
            // Child at 0 is CircleImageView
            assertThat(view.turbolinks_view.webViewRefresh?.getChildAt(1)).isEqualTo(webView)
        }
    }
}
