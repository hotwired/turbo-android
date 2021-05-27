package dev.hotwire.turbo.demo.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.hotwire.turbo.activities.TurboActivity
import dev.hotwire.turbo.delegates.TurboActivityDelegate
import dev.hotwire.turbo.demo.R
import kotlin.math.abs

class MainActivity : AppCompatActivity(), TurboActivity {
    override lateinit var delegate: TurboActivityDelegate

    val ONE_HOUR = 60 * 60 * 1000 // in ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurboActivityDelegate(this, R.id.main_nav_host)
    }

    override fun onPause() {
        super.onPause()

        getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE).edit()
            .putLong("APP_LAST_ENTERED_BACKGROUND", System.currentTimeMillis()).apply()
    }

    override fun onResume() {
        super.onResume()

        val lastResume = getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)
            .getLong("APP_LAST_ENTERED_BACKGROUND", System.currentTimeMillis())
        val diff = abs(System.currentTimeMillis() - lastResume)

        // if app last opened more than an hour ago, reload the last page open to ensure fresh content
        if (diff >= ONE_HOUR) {
            delegate.refresh()
        }
    }
}
