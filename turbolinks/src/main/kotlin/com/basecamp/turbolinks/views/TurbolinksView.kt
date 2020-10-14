package com.basecamp.turbolinks.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.contains
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.basecamp.turbolinks.R

/**
 * Turbolinks view
 *
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class TurbolinksView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private val webViewContainer: ViewGroup get() = findViewById(R.id.turbolinks_webView_container)
    private val progressContainer: ViewGroup get() = findViewById(R.id.turbolinks_progress_container)
    private val errorContainer: ViewGroup get() = findViewById(R.id.turbolinks_error_container)
    private val screenshotView: ImageView get() = findViewById(R.id.turbolinks_screenshot)

    internal val webViewRefresh: SwipeRefreshLayout? get() = webViewContainer as? SwipeRefreshLayout
    internal val errorRefresh: SwipeRefreshLayout? get() = findViewById(R.id.turbolinks_error_refresh)

    /**
     * Attach web view
     *
     * @param webView
     * @param onAttachedToNewDestination
     * @receiver
     */
    internal fun attachWebView(webView: WebView, onAttachedToNewDestination: (Boolean) -> Unit) {
        if (webView.parent != null) {
            onAttachedToNewDestination(false)
            return
        }

        // Match the WebView background with its new parent
        if (background is ColorDrawable) {
            webView.setBackgroundColor((background as ColorDrawable).color)
        }

        webViewContainer.post {
            webViewContainer.addView(webView)
            onAttachedToNewDestination(true)
        }
    }

    /**
     * Detach web view
     *
     * @param webView
     * @param onDetached
     * @receiver
     */
    internal fun detachWebView(webView: WebView, onDetached: () -> Unit) {
        // If the view is already detached from the window (like
        // when dismissing a bottom sheet), detach immediately,
        // since posting to the message queue will be ignored.
        if (webViewContainer.windowToken == null) {
            webViewContainer.removeView(webView)
            onDetached()
        } else {
            webViewContainer.post {
                webViewContainer.removeView(webView)
                onDetached()
            }
        }
    }

    /**
     * Web view is attached
     *
     * @param webView
     * @return
     */
    internal fun webViewIsAttached(webView: WebView): Boolean {
        return webViewContainer.contains(webView)
    }

    /**
     * Add progress view
     *
     * @param progressView
     */
    internal fun addProgressView(progressView: View) {
        // Don't show the progress view if a screenshot is available
        if (screenshotView.isVisible) return

        check(progressView.parent == null) { "Progress view cannot be attached to another parent" }

        removeProgressView()
        progressContainer.addView(progressView)
        progressContainer.isVisible = true
    }

    /**
     * Remove progress view
     *
     */
    internal fun removeProgressView() {
        progressContainer.removeAllViews()
        progressContainer.isVisible = false
    }

    /**
     * Add screenshot
     *
     * @param screenshot
     */
    internal fun addScreenshot(screenshot: Bitmap?) {
        if (screenshot == null) return

        screenshotView.setImageBitmap(screenshot)
        screenshotView.isVisible = true
    }

    /**
     * Remove screenshot
     *
     */
    internal fun removeScreenshot() {
        screenshotView.setImageBitmap(null)
        screenshotView.isVisible = false
    }

    /**
     * Add error view
     *
     * @param errorView
     */
    internal fun addErrorView(errorView: View) {
        check(errorView.parent == null) { "Error view cannot be attached to another parent" }

        removeErrorView()
        errorContainer.addView(errorView)
        errorContainer.isVisible = true

        errorRefresh?.let {
            it.isVisible = true
            it.isEnabled = true
            it.isRefreshing = false
        }
    }

    /**
     * Remove error view
     *
     */
    internal fun removeErrorView() {
        errorContainer.removeAllViews()
        errorContainer.isVisible = false

        errorRefresh?.let {
            it.isVisible = false
            it.isEnabled = false
            it.isRefreshing = false
        }
    }

    /**
     * Create screenshot
     *
     * @return
     */
    internal fun createScreenshot(): Bitmap? {
        if (!isLaidOut) return null
        if (!hasEnoughMemoryForScreenshot()) return null
        if (width <= 0 || height <= 0) return null

        // TODO: Catch-all approach where taking a screenshot for TL should never crash the app
        // https://sentry.io/organizations/basecamp/issues/1706905982/events/fbdca87b1f8d4bda882e946c1b890f88/?project=1861173&query=is%3Aunresolved
        return try {
            drawToBitmap()
        } catch (e: Exception) {
            // Don't ever crash when trying to make a screenshot
            null
        }
    }

    /**
     * Screenshot orientation
     *
     * @return
     */
    internal fun screenshotOrientation(): Int {
        return context.resources.configuration.orientation
    }

    private fun hasEnoughMemoryForScreenshot(): Boolean {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory().toFloat()
        val max = runtime.maxMemory().toFloat()
        val remaining = 1f - (used / max)

        // TODO: Some instances where low memory may be removing views and drawing bitmaps crashes
        // https://sentry.io/organizations/basecamp/issues/1706905982/events/latest/?project=1861173&query=is%3Aunresolved
        return remaining > .20
    }
}
