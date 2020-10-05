package com.basecamp.turbolinks.demosimple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basecamp.turbolinks.activity.TurbolinksActivityDelegate
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
