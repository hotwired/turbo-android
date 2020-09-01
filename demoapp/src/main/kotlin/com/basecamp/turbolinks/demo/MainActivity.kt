package com.basecamp.turbolinks.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.TurbolinksActivity
import com.basecamp.turbolinks.TurbolinksActivityDelegate
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    private val navHosts = listOf(
            R.id.food_nav_host,
            R.id.orders_nav_host,
            R.id.me_nav_host
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.food_nav_host).apply {
            registerNavHost(R.id.orders_nav_host)
            registerNavHost(R.id.me_nav_host)
        }

        initBottomTabsListener()
        verifyServerIpAddress(this)
    }

    fun animateBottomNavVisibility(fragment: Fragment, visible: Boolean) {
        if (fragment == delegate.currentDestination && visible != bottom_nav.isAlreadyVisible) {
            bottom_nav.animateVisibility(visible)
        }
    }

    private fun initBottomTabsListener() {
        bottom_nav.setOnNavigationItemReselectedListener { item ->
            delegate.clearBackStack()
        }

        bottom_nav.setOnNavigationItemSelectedListener { currentItem ->
            bottom_nav.menu.forEachIndexed { index, item ->
                val isCurrentItem = item == currentItem
                val navHostId = navHosts[index]

                findViewById<View>(navHostId).isVisible = isCurrentItem

                if (isCurrentItem) {
                    delegate.currentNavHostId = navHostId
                }
            }
            true
        }
    }
}
