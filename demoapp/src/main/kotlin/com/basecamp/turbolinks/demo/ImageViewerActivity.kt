package com.basecamp.turbolinks.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image_viewer.*

class ImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        loadImage()
    }

    private fun loadImage() {
        val location = intent?.getStringExtra("location") ?: return
        Glide.with(this).load(location).into(image_view)
    }
}
