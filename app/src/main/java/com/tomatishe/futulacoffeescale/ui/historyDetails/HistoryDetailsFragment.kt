package com.tomatishe.futulacoffeescale.ui.historyDetails

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.databinding.FragmentHistoryDetailsBinding
import com.tomatishe.futulacoffeescale.ui.historyExtra.HistoryExtraFragment
import com.tomatishe.futulacoffeescale.ui.historyInfo.HistoryInfoFragment

class ViewPager2Adapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val historyRecordId: Long
) :

    FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryInfoFragment.newInstance(historyRecordId)
            1 -> HistoryExtraFragment.newInstance(historyRecordId)
            else -> throw AssertionError()
        }
    }
}

class HistoryDetailsFragment : Fragment() {

    private var _binding: FragmentHistoryDetailsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = HistoryDetailsFragment()
    }

    private val viewModel: HistoryDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val viewPager2 = root.findViewById<ViewPager2>(R.id.pager)
        val historyRecordId = this.arguments?.getLong("historyRecordId")
        val pagerAdapter =
            historyRecordId?.let { ViewPager2Adapter(parentFragmentManager, lifecycle, it) }
        viewPager2.adapter = pagerAdapter
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val tabLayout = root.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager2) {tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.history_data_main)
                1 -> tab.text = getString(R.string.history_data_extra)
            }
        }.attach()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
