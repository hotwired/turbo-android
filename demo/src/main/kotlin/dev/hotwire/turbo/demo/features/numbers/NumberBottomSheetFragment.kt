package dev.hotwire.turbo.demo.features.numbers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.util.description
import dev.hotwire.turbo.fragments.TurboBottomSheetDialogFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/numbers/sheet")
class NumberBottomSheetFragment : TurboBottomSheetDialogFragment(), NavDestination {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_number_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        view.findViewById<MaterialTextView>(R.id.number).apply {
            text = Uri.parse(location).lastPathSegment
        }

        view.findViewById<MaterialTextView>(R.id.number_description).apply {
            text = pathProperties.description
        }
    }
}
