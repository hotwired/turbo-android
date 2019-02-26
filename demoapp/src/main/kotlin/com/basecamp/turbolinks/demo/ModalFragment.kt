package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_modal.*

class ModalFragment : WebFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_modal, container, false).also {
            delegate.createView(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toggleModalPresentation(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun onDetach() {
        toggleModalPresentation(false)
        super.onDetach()
    }

    override fun shouldEnablePullToRefresh(): Boolean {
        return false
    }

    private fun initView() {
        modal_close.setOnClickListener { delegate.activity?.navigateBack() }
        modal_submit.setOnClickListener { delegate.activity?.navigateBack() }
    }

    private fun toggleModalPresentation(modal: Boolean) {
        val view = activity?.findViewById<View>(R.id.bottom_nav) ?: return
        val startY = if (modal) 0 else view.height
        val endY = if (modal) view.height else 0

        view.translationYAnimator(
                startY = startY.toFloat(),
                endY = endY.toFloat(),
                duration = 200
        ).start()
    }
}
