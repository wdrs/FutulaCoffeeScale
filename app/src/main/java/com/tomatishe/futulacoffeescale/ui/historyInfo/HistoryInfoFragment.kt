package com.tomatishe.futulacoffeescale.ui.historyInfo

import android.content.res.Configuration
import android.content.res.Resources
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartStackingType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.tomatishe.futulacoffeescale.Dependencies
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.WeightRecordInfoTuple
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.DataCoordinator
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getFlowRateChartType
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getOneGraphInHistory
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getWeightChartType
import com.tomatishe.futulacoffeescale.databinding.FragmentHistoryInfoBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryInfoFragment : Fragment() {
    private fun String.replaceLast(oldValue: String, newValue: String): String {
        val lastIndex = lastIndexOf(oldValue)
        if (lastIndex == -1) {
            return this
        }
        val prefix = substring(0, lastIndex)
        val suffix = substring(lastIndex + oldValue.length)
        return "$prefix$newValue$suffix"
    }

    private var _binding: FragmentHistoryInfoBinding? = null
    private val binding get() = _binding!!

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

    private lateinit var chartFlowRateViewLayout: FrameLayout

    private var chartViewCategories = Array(3600) { i ->
        "%02d:%02d".format(i / 60, i - ((i / 60) * 60))
    }
    private var primaryChartColor: String = (if (isSystemDarkMode()) "#BB86FC" else "#6200EE")
    private var secondaryChartColor: String = (if (isSystemDarkMode()) "#C7DC86" else "#8CEE00")
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
    private var showOneGraphInHistory = false

    companion object {
        fun newInstance(historyRecordId: Long): Fragment {
            val fragment = HistoryInfoFragment()
            fragment.arguments = Bundle().apply {
                putLong("historyRecordId", historyRecordId)
            }
            return fragment
        }
    }

    private val viewModel: HistoryInfoViewModel by viewModels()

    private fun returnChartType(chartType: String): AAChartType {
        val returnChartType: AAChartType = when (chartType) {
            "Area" -> AAChartType.Area
            "AreaSpline" -> AAChartType.Areaspline
            "Column" -> AAChartType.Column
            "Line" -> AAChartType.Line
            "Spline" -> AAChartType.Spline
            else -> AAChartType.Areaspline
        }
        return returnChartType
    }

    private fun isSystemDarkMode(): Boolean {
        val configuration = Resources.getSystem().configuration
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private suspend fun drawAllData(
        weightList: MutableList<Float> = mutableListOf<Float>(),
        flowList: MutableList<Float> = mutableListOf<Float>()
    ) {
        val tmpWeightList = Array(weightList.size) { i ->
            0.00F
        }
        val tmpFlowList = Array(flowList.size) { i ->
            0.00F
        }
        weightLogGraphData =
            AASeriesElement().name("Weight").data(tmpWeightList.toMutableList().toTypedArray())
        flowRateLogGraphData =
            AASeriesElement().name("Flowrate").data(tmpFlowList.toMutableList().toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    weightLogGraphData, flowRateLogGraphData
                )
            )
        }
        delay(500)
        weightLogGraphData = AASeriesElement().name("Weight").data(weightList.toTypedArray())
        flowRateLogGraphData = AASeriesElement().name("Flowrate").data(flowList.toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    weightLogGraphData, flowRateLogGraphData
                )
            )
        }
    }

    private suspend fun drawFlowData(flowList: MutableList<Float> = mutableListOf<Float>()) {
        val tmpFlowList = Array(flowList.size) { i ->
            0.00F
        }
        flowRateLogGraphData =
            AASeriesElement().name("Flowrate").data(tmpFlowList.toMutableList().toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    flowRateLogGraphData
                )
            )
        }
        delay(500)
        flowRateLogGraphData = AASeriesElement().name("Flowrate").data(flowList.toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    flowRateLogGraphData
                )
            )
        }
    }

    private suspend fun drawWeightData(weightList: MutableList<Float> = mutableListOf<Float>()) {
        val tmpWeightList = Array(weightList.size) { i ->
            0.00F
        }
        weightLogGraphData =
            AASeriesElement().name("Weight").data(tmpWeightList.toMutableList().toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    weightLogGraphData
                )
            )
        }
        delay(500)
        weightLogGraphData = AASeriesElement().name("Weight").data(weightList.toTypedArray())
        Handler(Looper.getMainLooper()).post {
            chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    weightLogGraphData
                )
            )
        }
    }

    private suspend fun getRecordValues(recordId: Long) {
        val brewData: WeightRecordInfoTuple =
            Dependencies.weightRecordRepository.getOneWeightRecordDataById(recordId)
        doseRecord = brewData.doseRecord
        weightRecord = brewData.weightRecord
        weightUnit = brewData.weightUnit
        flowRateAvg = brewData.flowRateAvg
        flowRate = brewData.flowRate
        timeString = if (brewData.timeString.count { it == ':' } == 2) {
            brewData.timeString.replaceLast(":", ".")
        } else {
            brewData.timeString
        }
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

        val flt = flowRateLog.toMutableList()
        flt.retainAll { it >= 0.1F }
        flowRateAvg = if (flt.size > 0) {
            flt.average().toFloat()
        } else {
            0.0F
        }

        delay(1000)

        if (showOneGraphInHistory) {
            GlobalScope.launch {
                drawAllData(weightLog, flowRateLog)
            }
        } else {
            GlobalScope.launch {
                drawWeightData(weightLog)
            }
            GlobalScope.launch {
                drawFlowData(flowRateLog)
            }
        }
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

        weightText = binding.weightText
        doseText = binding.doseText
        flowRateText = binding.flowRateText

        weightLabel = binding.weightLabel
        doseLabel = binding.doseLabel
        flowRateLabel = binding.flowRateLabel

        flowRateText.setOnClickListener {
            if (showFlowRateAvg) {
                showFlowRateAvg = false
                flowRateLabel.text = getString(R.string.flowrate_text)
                flowRateText.text = "%.1f".format(flowRate)
            } else {
                showFlowRateAvg = true
                flowRateLabel.text = getString(R.string.flowrate_avg_text)
                flowRateText.text = "%.1f".format(flowRateAvg)
            }
        }

        timeText = binding.timeText
        brewRatioText = binding.brewRatioText

        chartWeightView = binding.chartWeightView
        chartFlowRateView = binding.chartFlowRateView

        chartFlowRateViewLayout = binding.chartFlowRateViewLayout


        weightLogGraphData =
            AASeriesElement().name(getString(R.string.weight_text)).data(weightLog.toTypedArray())
        flowRateLogGraphData = AASeriesElement().name(getString(R.string.flowrate_text))
            .data(flowRateLog.toTypedArray())

        GlobalScope.launch {
            showOneGraphInHistory = DataCoordinator.shared.getOneGraphInHistory()

            val weightChartType = DataCoordinator.shared.getWeightChartType()
            val flowRateChartType = DataCoordinator.shared.getFlowRateChartType()

            if (showOneGraphInHistory) {
                chartWeightViewModel = AAChartModel().chartType(returnChartType(weightChartType)).yAxisTitle(
                    getString(
                        R.string.weight_text
                    ) + " / " + getString(
                        R.string.flowrate_text
                    )
                )
                    .animationType(AAChartAnimationType.Elastic).tooltipEnabled(true)
                    .legendEnabled(true)
                    .dataLabelsEnabled(false)
                    .markerRadius(0)
                    .series(arrayOf(weightLogGraphData, flowRateLogGraphData))
                    .categories(chartViewCategories)
                    .colorsTheme(arrayOf(primaryChartColor, secondaryChartColor))
                    .backgroundColor("#00000000")

                Handler(Looper.getMainLooper()).post {
                    chartFlowRateViewLayout.visibility = View.GONE
                    chartWeightView.aa_drawChartWithChartModel(chartWeightViewModel)
                }
            } else {
                chartWeightViewModel = AAChartModel().chartType(returnChartType(weightChartType)).yAxisTitle(
                    getString(
                        R.string.weight_text
                    )
                )
                    .animationType(AAChartAnimationType.Elastic).tooltipEnabled(true)
                    .legendEnabled(false)
                    .markerRadius(0)
                    .dataLabelsEnabled(false).series(arrayOf(weightLogGraphData))
                    .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))
                    .backgroundColor("#00000000")
                chartFlowRateViewModel =
                    AAChartModel().chartType(returnChartType(flowRateChartType))
                        .yAxisTitle(getString(R.string.flowrate_text))
                        .animationType(AAChartAnimationType.Elastic).tooltipEnabled(true)
                        .legendEnabled(false).markerRadius(0).dataLabelsEnabled(false)
                        .series(arrayOf(flowRateLogGraphData))
                        .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))
                        .backgroundColor("#00000000")

                Handler(Looper.getMainLooper()).post {
                    chartWeightView.aa_drawChartWithChartModel(chartWeightViewModel)
                    chartFlowRateView.aa_drawChartWithChartModel(chartFlowRateViewModel)
                }
            }

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