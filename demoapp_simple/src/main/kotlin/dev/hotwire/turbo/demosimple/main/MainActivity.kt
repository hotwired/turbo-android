package dev.hotwire.turbo.demosimple.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.hotwire.turbo.activities.TurbolinksActivity
import dev.hotwire.turbo.delegates.TurbolinksActivityDelegate
import dev.hotwire.turbo.demosimple.R
import dev.hotwire.turbo.demosimple.util.verifyServerIpAddress

class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.main_nav_host)
        verifyServerIpAddress(this)
    }
}
