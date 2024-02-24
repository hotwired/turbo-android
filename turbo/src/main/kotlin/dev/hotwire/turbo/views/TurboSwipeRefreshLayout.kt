package dev.hotwire.turbo.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.children
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

internal class TurboSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        SwipeRefreshLayout(context, attrs) {

    init {
        disableCustomDrawingOrder()
    }

    override fun canChildScrollUp(): Boolean {
        val webView = children.firstOrNull() as? TurboWebView

        return if (webView != null) {
            webView.scrollY > 0 || webView.elementTouchIsScrollable
        } else {
            false
        }
    }

    /**
     * Disable custom child drawing order. This fixes a crash while using a
     * stylus that dispatches hover events when the WebView is being removed.
     * This doesn't have any unintended consequences, since the WebView is the
     * only possible child of this view.
     */
    private fun disableCustomDrawingOrder() {
        isChildrenDrawingOrderEnabled = false
    }
}
