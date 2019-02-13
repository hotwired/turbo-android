package com.basecamp.turbolinks.demo

import android.widget.FrameLayout
import androidx.navigation.NavController
import com.basecamp.turbolinks.TurbolinksSession

data class NavigationTab(
        val session: TurbolinksSession,
        val controller: NavController,
        val startDestination: Int,
        val menuId: Int,
        val section: FrameLayout
) {
    init {
        // Dynamically set the controller graph and start destination,
        // so we can use a shared, common navigation graph between tabs.
        controller.graph = controller.navInflater.inflate(R.navigation.nav_graph).apply {
            startDestination = this@NavigationTab.startDestination
        }
    }
}
