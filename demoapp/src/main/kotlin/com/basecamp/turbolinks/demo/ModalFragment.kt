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

    private fun initView() {
        modal_close.setOnClickListener { listener?.navigateBack() }
        modal_submit.setOnClickListener { listener?.navigateBack() }
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
