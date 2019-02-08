package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/* Internal view hierarchy:
 *
 * TurbolinksView
 *   > TurbolinksSwipeRefreshLayout
 *     > WebView (gets attached/detached here)
 *   > Progress View
 *   > Screenshot View
 */

class TurbolinksView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    internal val refreshLayout by lazy { addRefreshLayout() }

    private var progressView: View? = null
    private var screenshotView: ImageView? = null

    internal fun addWebView(webView: WebView): Boolean {
        if (webView.parent === refreshLayout) return false

        if (background is ColorDrawable) {
            webView.setBackgroundColor((background as ColorDrawable).color)
        }

        refreshLayout.addView(webView)
        return true
    }

    internal fun detachWebView(webView: WebView) {
        refreshLayout.removeView(webView)
    }

    internal fun addProgressView(progressView: View) {
        // Don't show the progress view if a screenshot is available
        if (screenshotView != null) return

        removeProgressView()
        if (progressView.parent != null) {
            throw IllegalStateException("Progress view cannot be attached to another parent")
        }

        this.progressView = progressView
        progressView.isClickable = true
        addView(progressView)
    }

    internal fun addScreenshotView(screenshot: Bitmap?) {
        if (screenshot == null) return

        screenshotView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            isClickable = true
            setImageBitmap(screenshot)
        }

        addView(screenshotView)
    }

    internal fun removeProgressView() {
        progressView?.let {
            removeView(it)
        }
    }

    internal fun removeScreenshotView() {
        screenshotView?.let {
            removeView(it)
            screenshotView = null
        }
    }

    private fun addRefreshLayout(): TurbolinksSwipeRefreshLayout {
        val layout = TurbolinksSwipeRefreshLayout(context, null)
        addView(layout, 0)

        return layout
    }

    fun createScreenshot(): Bitmap? {
        if (!hasEnoughMemoryForScreenshot()) return null
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap
    }

    fun screenshotOrientation(): Int {
        return context.resources.configuration.orientation
    }

    private fun hasEnoughMemoryForScreenshot(): Boolean {
        val runtime = Runtime.getRuntime()

        // Auto casting to floats necessary for division
        val used = runtime.totalMemory().toFloat()
        val max = runtime.maxMemory().toFloat()
        val remaining = 1f - (used / max)

        return remaining > .10
    }

    internal class TurbolinksSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
            SwipeRefreshLayout(context, attrs) {

        internal var callback: TurbolinksScrollUpCallback? = null

        override fun canChildScrollUp(): Boolean {
            return callback?.canChildScrollUp() ?: super.canChildScrollUp()
        }
    }
}
