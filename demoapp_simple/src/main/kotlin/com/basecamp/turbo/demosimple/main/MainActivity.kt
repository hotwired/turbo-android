package com.basecamp.turbo.demosimple.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basecamp.turbo.activities.TurbolinksActivity
import com.basecamp.turbo.delegates.TurbolinksActivityDelegate
import com.basecamp.turbo.demosimple.R
import com.basecamp.turbo.demosimple.util.verifyServerIpAddress

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.main_nav_host)
        verifyServerIpAddress(this)
    }
}
