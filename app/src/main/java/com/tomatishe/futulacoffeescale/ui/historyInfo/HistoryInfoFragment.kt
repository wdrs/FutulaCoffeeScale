package com.tomatishe.futulacoffeescale.ui.historyInfo

import android.content.res.Configuration
import android.content.res.Resources
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.tomatishe.futulacoffeescale.Dependencies
import com.tomatishe.futulacoffeescale.WeightRecordInfoTuple
import com.tomatishe.futulacoffeescale.databinding.FragmentHistoryInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HistoryInfoFragment : Fragment() {

    private var _binding: FragmentHistoryInfoBinding? = null
    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var weightText: TextView
    private lateinit var doseText: TextView
    private lateinit var weightLabel: TextView
    private lateinit var doseLabel: TextView
    private lateinit var flowRateLabel: TextView
    private lateinit var flowRateText: TextView
    private lateinit var timeText: TextView
    private lateinit var brewRatioText: TextView

    private lateinit var chartWeightView: AAChartView
    private lateinit var chartFlowRateView: AAChartView

    private var chartViewCategories = Array(3600) { i ->

        "%02d:%02d".format(i / 60, i - ((i / 60) * 60))
    }
    private var primaryChartColor: String = (if (isSystemDarkMode()) "#BB86FC" else "#6200EE")
    private var weightLog = mutableListOf<Float>()
    private var flowRateLog = mutableListOf<Float>()

    private lateinit var weightLogGraphData: AASeriesElement
    private lateinit var flowRateLogGraphData: AASeriesElement

    private lateinit var chartWeightViewModel: AAChartModel
    private lateinit var chartFlowRateViewModel: AAChartModel

    private var doseRecord: Float = 0.0F
        set(value) {
            field = value
            activity?.runOnUiThread {
                doseText.text = "%.1f".format(doseRecord)
            }
        }
    private var weightRecord: Float = 0.0F
        set(value) {
            field = value
            activity?.runOnUiThread {
                weightText.text = "%.1f".format(weightRecord)
            }
        }
    private var weightUnit: String = "g"
        set(value) {
            field = value
            activity?.runOnUiThread {
                weightLabel.text = "WEIGHT ($weightUnit)"
                doseLabel.text = "DOSE ($weightUnit)"
                if (showFlowRateAvg) {
                    flowRateLabel.text = "FLOW AVG ($weightUnit/s)"
                } else {
                    flowRateLabel.text = "FLOWRATE ($weightUnit/s)"
                }
            }
        }
    private var flowRate: Float = 0.0F
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (showFlowRateAvg) {
                    flowRateText.text = "%.1f".format(flowRateAvg)
                } else {
                    flowRateText.text = "%.1f".format(flowRate)
                }
            }
        }
    private var flowRateAvg: Float = 0.0F
    private var timeString: String = "00:00.0"
        set(value) {
            field = value
            activity?.runOnUiThread {
                timeText.text = timeString
            }
        }
    private var brewRatioString: String = "1:0,0"
        set(value) {
            field = value
            activity?.runOnUiThread {
                brewRatioText.text = brewRatioString
            }
        }
    private var showFlowRateAvg = true

    private var isGraphReady = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isGraphReady) {
                    chartWeightView.aa_drawChartWithChartModel(chartWeightViewModel)
                    chartFlowRateView.aa_drawChartWithChartModel(chartFlowRateViewModel)
                }
            }
        }

    companion object {
        fun newInstance() = HistoryInfoFragment()
    }

    private val viewModel: HistoryInfoViewModel by viewModels()

    private fun isSystemDarkMode(): Boolean {
        val configuration = Resources.getSystem().configuration
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private suspend fun getRecordValues(recordId: Long) {
        val brewData: WeightRecordInfoTuple =
            Dependencies.weightRecordRepository.getOneWeightRecordDataById(recordId)
        doseRecord = brewData.doseRecord
        weightRecord = brewData.weightRecord
        weightUnit = brewData.weightUnit
        flowRateAvg = brewData.flowRateAvg
        flowRate = brewData.flowRate
        timeString = brewData.timeString
        brewRatioString = brewData.brewRatioString

        try {
            weightLog = brewData.weightLog.split(";").map(String::toFloat).toMutableList()
            weightLog.replaceAll { if (it < 0) 0.0F else it }
        } catch (exception: Exception) {
            weightLog = mutableListOf<Float>()
        }
        try {
            flowRateLog = brewData.flowRateLog.split(";").map(String::toFloat).toMutableList()
        } catch (exception: Exception) {
            flowRateLog = mutableListOf<Float>()
        }

        weightLogGraphData = AASeriesElement().name("Weight").data(weightLog.toTypedArray())
        flowRateLogGraphData = AASeriesElement().name("Flowrate").data(flowRateLog.toTypedArray())

        chartWeightViewModel = AAChartModel().chartType(AAChartType.Areaspline).yAxisTitle("Weight")
            .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false).legendEnabled(false)
            .dataLabelsEnabled(false).series(arrayOf(weightLogGraphData))
            .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))
        chartFlowRateViewModel =
            AAChartModel().chartType(AAChartType.Areaspline).yAxisTitle("Flow rate")
                .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false)
                .legendEnabled(false).dataLabelsEnabled(false).series(arrayOf(flowRateLogGraphData))
                .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))

        isGraphReady = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val historyRecordId = this.arguments?.getLong("historyRecordId")

        chartWeightView = binding.chartWeightView
        chartFlowRateView = binding.chartFlowRateView

        chartWeightView.isClearBackgroundColor = true
        chartFlowRateView.isClearBackgroundColor = true

        /*        chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                    arrayOf(
                        flowRateLogGraphData
                    )
                )

                chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                    arrayOf(
                        weightLogGraphData
                    )
                )*/

        weightText = binding.weightText
        doseText = binding.doseText
        flowRateText = binding.flowRateText

        weightLabel = binding.weightLabel
        doseLabel = binding.doseLabel
        flowRateLabel = binding.flowRateLabel

        flowRateText.setOnClickListener {
            if (showFlowRateAvg) {
                showFlowRateAvg = false
                flowRateLabel.text = "FLOWRATE ($weightUnit/s)"
                flowRateText.text = "%.1f".format(flowRate)
            } else {
                showFlowRateAvg = true
                flowRateLabel.text = "FLOW AVG ($weightUnit/s)"
                flowRateText.text = "%.1f".format(flowRateAvg)
            }
        }

        timeText = binding.timeText
        brewRatioText = binding.brewRatioText

        isGraphReady = false

        scope.launch {
            if (historyRecordId != null) {
                getRecordValues(historyRecordId)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}