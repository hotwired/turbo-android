package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.basecamp.turbolinks.TurbolinksActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.android.synthetic.main.fragment_me.view.*

class MeFragment : Fragment() {
    private var listener: TurbolinksActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        when (context) {
            is TurbolinksActivity -> listener = context
            else -> throw RuntimeException("$context must implement OnFragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        loadAvatar()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun initView() {
        me_edit_profile.setOnClickListener {
            listener?.navigate(Constants.PROFILE_EDIT_URL, "advance")
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
