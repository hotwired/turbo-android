package dev.hotwire.turbo.observers

import android.content.res.Resources.Theme
import android.os.Build
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowInsetsController
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dev.hotwire.turbo.nav.TurboNavDestination

internal class TurboWindowThemeObserver(val destination: TurboNavDestination) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun updateSystemBarColors() {
        val view = destination.fragment.view ?: return
        updateWindowTheme(view.context.theme)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun restoreSystemBarColors() {
        val activity = destination.fragment.activity ?: return
        updateWindowTheme(activity.theme)
    }

    private fun updateWindowTheme(theme: Theme) {
        updateStatusBar(theme)
        updateNavigationBar(theme)
    }

    private fun updateStatusBar(theme: Theme) {
        val window = destination.fragment.requireActivity().window
        val statusBarColor = colorAttribute(theme, android.R.attr.statusBarColor)
        val useLightStatusBar = booleanAttribute(theme, android.R.attr.windowLightStatusBar)

        window.statusBarColor = statusBarColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            updateSystemBarsAppearance(useLightStatusBar)
        } else {
            @Suppress("DEPRECATION")
            updateSystemUiVisibility(useLightStatusBar, SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }


    private fun updateNavigationBar(theme: Theme) {
        val window = destination.fragment.requireActivity().window
        val navigationBarColor = colorAttribute(theme, android.R.attr.navigationBarColor)

        window.navigationBarColor = navigationBarColor

        // Light navigation bars are only available in API 27+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return
        }

        val useLightNavigationBar = booleanAttribute(theme, android.R.attr.windowLightNavigationBar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            updateSystemBarsAppearance(useLightNavigationBar)
        } else {
            @Suppress("DEPRECATION")
            updateSystemUiVisibility(useLightNavigationBar, SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
    }

    private fun updateSystemBarsAppearance(useLightSystemBars: Boolean) {
        val window = destination.fragment.requireActivity().window
        val appearance = when (useLightSystemBars) {
            true -> WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            else -> 0
        }

        // Even though this is deprecated, we still need to clear out the
        // theme flags before updating the system bars appearance.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        window.insetsController?.setSystemBarsAppearance(
            appearance,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    }

    @Suppress("DEPRECATION")
    private fun updateSystemUiVisibility(useLightSystemBar: Boolean, flag: Int) {
        val window = destination.fragment.requireActivity().window

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
