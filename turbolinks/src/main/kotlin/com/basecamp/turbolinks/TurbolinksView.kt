package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.turbolinks_view.view.*

class TurbolinksView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    internal val refreshLayout get() = turbolinks_refresh
    internal val errorRefreshLayout get() = turbolinks_error_refresh

    internal fun attachWebView(webView: WebView): Boolean {
        if (webView.parent == turbolinks_refresh) return false

        // Match the WebView background with its new parent
        if (background is ColorDrawable) {
            webView.setBackgroundColor((background as ColorDrawable).color)
        }

        turbolinks_refresh.addView(webView)
        return true
    }

    internal fun detachWebView(webView: WebView) {
        turbolinks_refresh.removeView(webView)
    }

    internal fun addProgressView(progressView: View) {
        // Don't show the progress view if a screenshot is available
        if (turbolinks_screenshot.isVisible) return

        check(progressView.parent == null) { "Progress view cannot be attached to another parent" }

        removeProgressView()
        turbolinks_progress.addView(progressView)
        turbolinks_progress.isVisible = true
    }

    internal fun removeProgressView() {
        turbolinks_progress.removeAllViews()
        turbolinks_progress.isVisible = false
    }

    internal fun addScreenshot(screenshot: Bitmap?) {
        if (screenshot == null) return

        turbolinks_screenshot.setImageBitmap(screenshot)
        turbolinks_screenshot.isVisible = true
    }

    internal fun removeScreenshot() {
        turbolinks_screenshot.setImageBitmap(null)
        turbolinks_screenshot.isVisible = false
    }

    internal fun addErrorView(errorView: View) {
        check(errorView.parent == null) { "Error view cannot be attached to another parent" }

        turbolinks_error_refresh.addView(errorView)
        turbolinks_error_refresh.isVisible = true
        turbolinks_error_refresh.isEnabled = true
        turbolinks_error_refresh.isRefreshing = false
    }

    internal fun removeErrorView() {
        turbolinks_error_refresh.removeAllViews()
        turbolinks_error_refresh.isVisible = false
        turbolinks_error_refresh.isEnabled = false
        turbolinks_error_refresh.isRefreshing = false
    }

    fun createScreenshot(): Bitmap? {
        if (!isLaidOut) return null
        if (!hasEnoughMemoryForScreenshot()) return null
        if (width <= 0 || height <= 0) return null

        return drawToBitmap()
    }

    fun screenshotOrientation(): Int {
        return context.resources.configuration.orientation
    }

    private fun hasEnoughMemoryForScreenshot(): Boolean {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory().toFloat()
        val max = runtime.maxMemory().toFloat()
        val remaining = 1f - (used / max)

        return remaining > .10
    }
}
