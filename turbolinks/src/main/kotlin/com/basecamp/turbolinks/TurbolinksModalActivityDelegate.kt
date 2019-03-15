package com.basecamp.turbolinks

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.basecamp.turbolinks.PresentationContext.MODAL

class TurbolinksModalActivityDelegate(activity: TurbolinksActivity) : TurbolinksActivityDelegate(activity) {
    // ----------------------------------------------------------------------------
    // TurbolinksActivity interface
    // ----------------------------------------------------------------------------

    override fun navigate(location: String, action: String) {
        val presentationContext = onProvideRouter().getPresentationContext(location)
        val presentation = presentation(location, action)

        when (presentationContext) {
            MODAL -> navigateWithinContext(location, presentation)
            else -> dismissModalContext(location, action)
        }
    }

    private fun dismissModalContext(location: String, action: String) {
        val context = activity as Activity
        val intent = Intent().apply {
            putExtra("locationResult", location)
            putExtra("actionResult", action)
        }

        context.setResult(RESULT_OK, intent)
        context.finish()
    }
}
