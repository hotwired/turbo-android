package com.basecamp.turbolinks.demosimple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.basecamp.turbolinks.fragment.TurbolinksFragment
import com.basecamp.turbolinks.util.TurbolinksNavGraphDestination
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_image_viewer.*

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/image_viewer")
class ImageViewerFragment : TurbolinksFragment(), Destination {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadImage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_viewer, container, false)
    }

    override fun toolbarForNavigation(): Toolbar? {
        return null
    }

    private fun loadImage() {
        Glide.with(this).load(location).into(image_view)
    }
}
