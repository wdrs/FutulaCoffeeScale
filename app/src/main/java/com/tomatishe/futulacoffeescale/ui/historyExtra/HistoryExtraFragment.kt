package com.tomatishe.futulacoffeescale.ui.historyExtra

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomatishe.futulacoffeescale.R

class HistoryExtraFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryExtraFragment()
    }

    private val viewModel: HistoryExtraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history_extra, container, false)
    }
}