package com.basecamp.turbolinks.demo

import android.content.Context
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.TurbolinksActivity

abstract class NativeFragment : Fragment() {
    protected var listener: TurbolinksActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as? TurbolinksActivity ?:
                throw RuntimeException("The fragment Activity must implement TurbolinksActivity")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
