package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.basecamp.turbolinks.TurbolinksNavGraphDestination
import kotlinx.android.synthetic.main.fragment_web_modal.*

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/web/modal")
class WebModalFragment : WebFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_modal, container, false)
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
        modal_close.setOnClickListener { navigateBack() }
        modal_submit.setOnClickListener { navigateBack() }
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
