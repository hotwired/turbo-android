package dev.hotwire.turbo.demo.features.imageviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.fragments.TurboFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination
import com.bumptech.glide.Glide
import dev.hotwire.turbo.demo.util.drawable
import kotlinx.android.synthetic.main.fragment_image_viewer.*

@TurboNavGraphDestination(uri = "turbo://fragment/image_viewer")
class ImageViewerFragment : TurboFragment(), NavDestination {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
        loadImage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_viewer, container, false)
    }

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    private fun initToolbar() {
        toolbarForNavigation()?.navigationIcon =  context?.drawable(R.drawable.ic_close)
    }

    private fun loadImage() {
        Glide.with(this).load(location).into(image_view)
    }
}
