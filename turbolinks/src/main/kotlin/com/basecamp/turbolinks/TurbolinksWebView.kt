package com.basecamp.turbolinks

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.webkit.WebViewCompat
import com.google.gson.GsonBuilder

@SuppressLint("SetJavaScriptEnabled")
open class TurbolinksWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {
    private val gson = GsonBuilder().disableHtmlEscaping().create()

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    val packageName: String?
        get() = WebViewCompat.getCurrentWebViewPackage(context)?.packageName

    val versionName: String?
        get() = WebViewCompat.getCurrentWebViewPackage(context)?.versionName

    val majorVersion: Int?
        get() = versionName?.substringBefore(".")?.toIntOrNull()

    internal fun visitLocation(location: String, options: VisitOptions, restorationIdentifier: String) {
        val args = encodeArguments(location, options.toJson(), restorationIdentifier)
        runJavascript("webView.visitLocationWithOptionsAndRestorationIdentifier($args)")
    }

    internal fun visitRenderedForColdBoot(coldBootVisitIdentifier: String) {
        runJavascript("webView.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
    }

    internal fun installBridge(onBridgeInstalled: () -> Unit) {
        val script = "window.webView == null"
        val bridge = context.contentFromAsset("js/turbolinks_bridge.js")

        runJavascript(script) { s ->
            if (s?.toBoolean() == true) {
                runJavascript(bridge) {
                    onBridgeInstalled()
                }
            }
        }
    }

    private fun WebView.runJavascript(javascript: String, onComplete: (String?) -> Unit = {}) {
        context.runOnUiThread {
            evaluateJavascript(javascript) {
                onComplete(it)
            }
        }
    }

    private fun encodeArguments(vararg args: Any): String? {
        return args.joinToString(",") { gson.toJson(it) }
    }
}
