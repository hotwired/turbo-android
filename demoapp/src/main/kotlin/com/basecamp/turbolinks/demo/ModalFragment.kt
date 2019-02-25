package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_modal.*

class ModalFragment : WebFragment() {
    override val pullToRefreshEnabled = false

    override fun createView(): View {
        return layoutInflater.inflate(R.layout.fragment_modal, null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener?.onRequestEnterModalPresentation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun onDetach() {
        listener?.onRequestExitModalPresentation()
        super.onDetach()
    }

    private fun initView() {
        modal_close.setOnClickListener { listener?.popBackStack() }
        modal_submit.setOnClickListener { listener?.popBackStack() }
    }
}
