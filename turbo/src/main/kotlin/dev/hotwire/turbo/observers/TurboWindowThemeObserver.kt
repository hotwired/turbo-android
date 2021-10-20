package dev.hotwire.turbo.observers

import android.content.res.Resources.Theme
import android.os.Build
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.Window
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.util.animateColorTo

internal class TurboWindowThemeObserver(val destination: TurboNavDestination) : LifecycleObserver {
    private val window: Window
        get() = destination.fragment.requireActivity().window

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun updateSystemBarColors() {
        val view = destination.fragment.view ?: return
        val theme = view.context.theme

        updateStatusBar(theme)
        updateNavigationBar(theme)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun restoreSystemBarColors() {
        val activity = destination.fragment.activity ?: return
        val theme = activity.theme

        updateStatusBar(theme)
        updateNavigationBar(theme)
    }

    private fun updateStatusBar(theme: Theme) {
        val statusBarColor = colorAttribute(theme, android.R.attr.statusBarColor)
        val useLightStatusBar = booleanAttribute(theme, android.R.attr.windowLightStatusBar)

        window.statusBarColor.animateColorTo(statusBarColor) {
            window.statusBarColor = it
        }

        @Suppress("DEPRECATION")
        updateSystemBar(useLightStatusBar, SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }


    private fun updateNavigationBar(theme: Theme) {
        val navigationBarColor = colorAttribute(theme, android.R.attr.navigationBarColor)

        window.navigationBarColor.animateColorTo(navigationBarColor) {
            window.navigationBarColor = it
        }

        // Light navigation bars are only available in API 27+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return
        }

        val useLightNavigationBar = booleanAttribute(theme, android.R.attr.windowLightNavigationBar)

        @Suppress("DEPRECATION")
        updateSystemBar(useLightNavigationBar, SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
    }

    private fun updateSystemBar(useLightSystemBar: Boolean, flag: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            updateSystemBarsAppearance(useLightSystemBar)
        } else {
            updateSystemUiVisibility(useLightSystemBar, flag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateSystemBarsAppearance(useLightSystemBars: Boolean) {
        val appearance = when (useLightSystemBars) {
            true -> APPEARANCE_LIGHT_STATUS_BARS
            else -> 0
        }

        // Even though this is deprecated, we still need to clear out the
        // theme flags before updating the system bars appearance.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        window.insetsController?.setSystemBarsAppearance(
            appearance,
            APPEARANCE_LIGHT_STATUS_BARS
        )
    }

    @Suppress("DEPRECATION")
    private fun updateSystemUiVisibility(useLightSystemBar: Boolean, flag: Int) {
        val flags = when (useLightSystemBar) {
            true -> window.decorView.systemUiVisibility or flag
            else -> window.decorView.systemUiVisibility and flag.inv()
        }

        window.decorView.systemUiVisibility = flags
    }

    private fun colorAttribute(theme: Theme, attr: Int): Int {
        val attrs = theme.obtainStyledAttributes(intArrayOf(attr))

        return attrs.getColor(0, 0).apply {
            attrs.recycle()
        }
    }

    private fun booleanAttribute(theme: Theme, attr: Int): Boolean {
        val attrs = theme.obtainStyledAttributes(intArrayOf(attr))

        return attrs.getBoolean(0, false).apply {
            attrs.recycle()
        }
    }
}
