package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import com.basecamp.turbolinks.TurbolinksFragment
import kotlinx.android.synthetic.main.fragment_form.*

class FormFragment : TurbolinksFragment(), NavigationFragment {
    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_form, null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener?.onRequestFullscreen()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun onDetach() {
        listener?.onRequestExitFullscreen()
        super.onDetach()
    }

    override fun initialUrl(): String {
        throw IllegalStateException("A location parameter must be provided")
    }

    override fun provideTitle(): String {
        return title()
    }

    private fun initView() {
        form_close.setOnClickListener { listener?.popBackStack() }
        form_submit.setOnClickListener { listener?.popBackStack() }
    }
}
