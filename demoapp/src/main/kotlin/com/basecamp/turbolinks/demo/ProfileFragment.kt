package com.basecamp.turbolinks.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.basecamp.turbolinks.util.TurbolinksNavGraphDestination
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.android.synthetic.main.fragment_me.view.*

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/profile")
class ProfileFragment : NativeFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        loadAvatar()
    }

    override fun toolbarForNavigation(): Toolbar? {
        return null
    }

    private fun initView() {
        me_edit_profile.setOnClickListener {
            navigate(Constants.PROFILE_EDIT_URL)
        }
    }

    private fun loadAvatar() {
        val avatarView = view?.me_avatar ?: return

        val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_me)
                .transform(CircleCrop())

        Glide.with(this)
                .load(Constants.AVATAR_URL)
                .apply(requestOptions)
                .into(avatarView)
    }
}
