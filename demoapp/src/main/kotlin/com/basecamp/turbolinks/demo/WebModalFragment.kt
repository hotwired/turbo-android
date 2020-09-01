package com.basecamp.turbolinks.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import com.basecamp.turbolinks.TurbolinksNavGraphDestination

@TurbolinksNavGraphDestination(uri = "turbolinks://fragment/web/modal")
class WebModalFragment : WebFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toggleModalPresentation(true)
    }

    override fun onDetach() {
        toggleModalPresentation(false)
        super.onDetach()
    }

    override fun shouldEnablePullToRefresh(): Boolean {
        return false
    }

    private fun initToolbar() {
        toolbarForNavigation()?.navigationIcon = context?.drawable(R.drawable.ic_close)
        toolbarForNavigation()?.navigationContentDescription = getString(R.string.modal_close)
    }

    private fun toggleModalPresentation(modal: Boolean) {
        val view = activity?.findViewById<View>(R.id.bottom_nav) ?: return
        val startY = if (modal) 0 else view.height
        val endY = if (modal) view.height else 0

        view.translationYAnimator(
                startY = startY.toFloat(),
                endY = endY.toFloat()
        ).start()
    }
}
