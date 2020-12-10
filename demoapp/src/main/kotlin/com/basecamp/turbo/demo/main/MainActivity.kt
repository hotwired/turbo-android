package com.basecamp.turbo.demo.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.basecamp.turbo.activities.TurbolinksActivity
import com.basecamp.turbo.delegates.TurbolinksActivityDelegate
import com.basecamp.turbo.demo.R
import com.basecamp.turbo.demo.extensions.animateVisibility
import com.basecamp.turbo.demo.extensions.isAlreadyVisible
import com.basecamp.turbo.demo.util.verifyServerIpAddress
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    private val navHostFragments = listOf(
        R.id.food_nav_host,
        R.id.orders_nav_host,
        R.id.me_nav_host
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.food_nav_host).apply {
            registerNavHostFragment(R.id.orders_nav_host)
            registerNavHostFragment(R.id.me_nav_host)
        }

        initBottomTabsListener()
        verifyServerIpAddress(this)
    }

    fun animateBottomNavVisibility(fragment: Fragment, visible: Boolean) {
        if (fragment == delegate.currentNavDestination && visible != bottom_nav.isAlreadyVisible) {
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
                val navHostFragmentId = navHostFragments[index]

                findViewById<View>(navHostFragmentId).isVisible = isCurrentItem

                if (isCurrentItem) {
                    delegate.currentNavHostFragmentId = navHostFragmentId
                }
            }
            true
        }
    }
}
