package dev.hotwire.turbo.demo.features.numbers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import dev.hotwire.turbo.demo.R
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.demo.util.title
import dev.hotwire.turbo.fragments.TurboFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination
import kotlinx.android.synthetic.main.fragment_numbers.view.*

@TurboNavGraphDestination(uri = "turbo://fragment/numbers")
class NumbersFragment : TurboFragment(), NavDestination {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_numbers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initList(view)
    }

    private fun initToolbar() {
        fragmentViewModel.setTitle(pathProperties.title ?: "")
    }

    private fun initList(view: View) {
        view.recycler_view.layoutManager = LinearLayoutManager(view.context)
        view.recycler_view.adapter = NumbersAdapter().apply {
            setData((1..100).toList())
        }
    }
}
