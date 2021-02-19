package dev.hotwire.turbo.observers

import android.webkit.CookieManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

internal class TurboActivityObserver : LifecycleObserver {
    /**
     * Cookies may not be persisted to storage yet, since WebView
     * maintains its own internal timing to flush in-memory cookies
     * to persistent storage. Ensure that cookies are maintained
     * across app restarts.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun persistWebViewCookies() {
        CookieManager.getInstance().flush()
    }
}
