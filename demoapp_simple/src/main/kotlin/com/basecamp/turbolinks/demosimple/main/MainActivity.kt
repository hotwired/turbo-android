package com.basecamp.turbolinks.demosimple.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basecamp.turbolinks.delegates.TurbolinksActivityDelegate
import com.basecamp.turbolinks.demosimple.R
import com.basecamp.turbolinks.demosimple.util.verifyServerIpAddress
import com.basecamp.turbolinks.util.TurbolinksActivity

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.main_nav_host)
        verifyServerIpAddress(this)
    }
}
