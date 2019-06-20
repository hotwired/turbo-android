package com.basecamp.turbolinks.demo

import android.widget.FrameLayout
import androidx.navigation.NavController
import com.basecamp.turbolinks.TurbolinksActivityDelegate
import com.basecamp.turbolinks.TurbolinksSession

data class NavigationTab(
        val delegate: TurbolinksActivityDelegate,
        val session: TurbolinksSession,
        val controller: NavController,
        val startLocation: String,
        val startDestination: Int,
        val menuId: Int,
        val section: FrameLayout
) {
    init {
        // Dynamically set the controller graph and start destination,
        // so we can use a shared, common navigation graph between tabs.
        delegate.startControllerGraph(
                controller = controller,
                startLocation = startLocation,
                navGraph = R.navigation.nav_graph,
                startDestination = startDestination
        )
    }
}
