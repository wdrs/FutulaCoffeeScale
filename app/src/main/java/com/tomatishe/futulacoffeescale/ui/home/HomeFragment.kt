package com.tomatishe.futulacoffeescale.ui.home

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.material.materialswitch.MaterialSwitch
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.databinding.FragmentHomeBinding
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.WriteType
import com.welie.blessed.asUInt8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timerx.buildStopwatch
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val SCALE_NAME = "LFSmart Scale"
private const val scaleServiceUuidString = "0000fff0-0000-1000-8000-00805f9b34fb"
private const val scaleSendCommandUuidString = "0000fff1-0000-1000-8000-00805f9b34fb"
private const val scaleGetWeightUuidString = "0000fff4-0000-1000-8000-00805f9b34fb"
private const val scaleBatteryServiceUuidString = "0000180f-0000-1000-8000-00805f9b34fb"
private const val scaleBatteryLevelUuidString = "00002a19-0000-1000-8000-00805f9b34fb"
private const val resetCommand = "fd320000000000000000cf"
private const val unitGramCommand = "fd000400000000000000f9"
private const val unitOzCommand = "fd000600000000000000fb"
private const val unitMlWCommand = "fd000700000000000000fa"
private const val unitMlMilkCommand = "fd000800000000000000f5"
// private const val deviceNameUuidString = "00002a00-0000-1000-8000-00805f9b34fb"
// private const val deviceManufacturerNameUuidString = "00002a29-0000-1000-8000-00805f9b34fb"
// private const val deviceSerialNumberUuidString = "00002a25-0000-1000-8000-00805f9b34fb"
// private const val deviceSoftwareRevisionUuidString = "00002a28-0000-1000-8000-00805f9b34fb"
// private const val deviceFirmwareRevisionUuidString = "00002a26-0000-1000-8000-00805f9b34fb"
// private const val deviceSystemIDUuidString = "00002a23-0000-1000-8000-00805f9b34fb"

class HomeFragment : Fragment() {
    private var mRootView: View? = null
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var batteryLevel: Int = 100
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
                waitWatch.reset()
                waitWatch.start()
                weightText.text = "%.1f".format(weightRecord)
                if (autoStartSwitch.isChecked && doseRecord > 0 && !isTimerWorking && weightRecord > 0.1) {
                    startStopWatch()
                }
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
    private var weightLog = mutableListOf<Float>()
    private var weightLogSecond = mutableListOf<Float>()
    private var weightLogGraphData =
        AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
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
    private var flowRateLog = mutableListOf<Float>()
    private var flowRateLogSecond = mutableListOf<Float>()
    private var flowRateLogGraphData =
        AASeriesElement().name("Flowrate").data(flowRateLogSecond.toTypedArray())
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

    private var chartViewCategories = Array(3600) { i ->
        "%02d:%02d".format(i / 60, i - ((i / 60) * 60))
    }

    private var primaryChartColor: String = (if (isSystemDarkMode()) "#BB86FC" else "#6200EE")

    private var chartWeightViewModel: AAChartModel =
        AAChartModel().chartType(AAChartType.Areaspline).yAxisTitle("Weight")
            .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false).legendEnabled(false)
            .dataLabelsEnabled(false).series(arrayOf(weightLogGraphData))
            .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))

    private var chartFlowRateViewModel: AAChartModel =
        AAChartModel().chartType(AAChartType.Areaspline).yAxisTitle("Flow rate")
            .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false).legendEnabled(false)
            .dataLabelsEnabled(false).series(arrayOf(flowRateLogGraphData))
            .categories(chartViewCategories).colorsTheme(arrayOf(primaryChartColor))

    private var currentScanButtonText: String = "CONNECT"
        set(value) {
            field = value
            activity?.runOnUiThread {
                scanButton.text = currentScanButtonText
            }
        }
    private lateinit var scanButton: Button
    private lateinit var resetButton: Button
    private lateinit var doseButton: Button
    private lateinit var weightText: TextView
    private lateinit var doseText: TextView
    private lateinit var weightLabel: TextView
    private lateinit var doseLabel: TextView
    private lateinit var flowRateLabel: TextView
    private lateinit var flowRateText: TextView
    private lateinit var timeText: TextView
    private lateinit var brewRatioText: TextView
    private lateinit var autoStartSwitch: MaterialSwitch
    private lateinit var autoTareSwitch: MaterialSwitch
    private lateinit var autoDoseSwitch: MaterialSwitch
    private lateinit var chartWeightView: AAChartView
    private lateinit var chartFlowRateView: AAChartView
    private lateinit var scalePeripheral: BluetoothPeripheral
    private lateinit var central: BluetoothCentralManager

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        isScanning = true
        central.scanForPeripheralsUsingFilters(filters = listOf(scanFilter),
            { peripheral, scanResult ->
                Log.d(
                    "INFO", "Found peripheral '${peripheral.name}' with RSSI ${scanResult.rssi}"
                )
                isScanning = false
                scalePeripheral = peripheral
                central.stopScan()
                scope.launch { connectToScale(scalePeripheral) }
            },
            { scanFailure ->
                Log.d("ERROR", "Scan failed with reason $scanFailure")
                isScanning = false
            })

    }

    private fun intFrom2ByteArray(value: ByteArray): Int {
        if (value.size != 2) return 0
        return (value[1].toInt() and 0xff shl 8) or (value[0].toInt() and 0xff)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun sendCommandToScale(peripheral: BluetoothPeripheral, value: String) {
        try {
            peripheral.getCharacteristic(
                UUID.fromString(scaleServiceUuidString), UUID.fromString(
                    scaleSendCommandUuidString
                )
            )?.let {
                peripheral.writeCharacteristic(
                    it, value.hexToByteArray(HexFormat.Default), WriteType.WITH_RESPONSE
                )
            }
        } catch (exception: Exception) {
            Log.d("ERROR", "Connection to ${peripheral.name} failed")
            isConnected = false
            isScanning = false
            currentScanButtonText = "CONNECT"
        }
    }

    private suspend fun connectToScale(peripheral: BluetoothPeripheral) {
        currentScanButtonText = "CONNECTING"
        try {
            central.connectPeripheral(peripheral)
            isConnected = true
        } catch (exception: Exception) {
            Log.d("ERROR", "Connection to ${peripheral.name} failed")
            isConnected = false
            isScanning = false
            currentScanButtonText = "CONNECT"
        }
        sendCommandToScale(peripheral, unitGramCommand)
        try {
            peripheral.getCharacteristic(
                UUID.fromString(scaleServiceUuidString), UUID.fromString(
                    scaleGetWeightUuidString
                )
            )?.let {
                peripheral.observe(it) { value ->
                    val weightSign = if (value.sliceArray(5..5)[0].toInt() > 0) -1.0 else 1.0
                    val localWeightUnit = value.sliceArray(8..8)[0].toInt()
                    weightUnit = when (localWeightUnit) {
                        4 -> "g"
                        6 -> "oz"
                        7 -> "ml w"
                        8 -> "ml milk"
                        else -> {
                            "g"
                        }
                    }
                    val weightUnitMultiplier = when (localWeightUnit) {
                        4 -> 1.0
                        6 -> 0.035274
                        7 -> 1.0
                        8 -> 0.972
                        else -> {
                            1.0
                        }
                    }
                    weightRecord =
                        (intFrom2ByteArray(value.sliceArray(3..4)).toFloat() / 10) * weightSign.toFloat() * weightUnitMultiplier.toFloat()
                }
            }
            peripheral.getCharacteristic(
                UUID.fromString(scaleBatteryServiceUuidString), UUID.fromString(
                    scaleBatteryLevelUuidString
                )
            )?.let {
                batteryLevel = peripheral.readCharacteristic(it).asUInt8()!!.toInt()
                Log.d("INFO", "BATTERY = $batteryLevel")
                peripheral.observe(it) { value ->
                    Log.d("INFO", "BATTERY = $value")
                }
            }
        } catch (exception: Exception) {
            Log.d("ERROR", "Connection to ${peripheral.name} failed")
            isConnected = false
            isScanning = false
            currentScanButtonText = "CONNECT"
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        central.stopScan()
        isScanning = false
    }

    private val scanFilter = ScanFilter.Builder().setDeviceName(SCALE_NAME).build()

    private val waitWatch = buildStopwatch {
        startFormat("MM:SS")
        useExactDelay(true)
        actionWhen(1, TimeUnit.SECONDS) {
            if (autoDoseSwitch.isChecked && !autoTareSwitch.isChecked && !isTimerWorking && weightRecord > 0.1) {
                doseRecord = weightRecord
                scope.launch {
                    sendCommandToScale(
                        scalePeripheral, resetCommand
                    )
                }
                autoDoseSwitch.isChecked = false
            }
            if (autoTareSwitch.isChecked && !isTimerWorking && weightRecord > 0.1) {
                weightRecord = 0.0F
                scope.launch {
                    sendCommandToScale(
                        scalePeripheral, resetCommand
                    )
                }
                autoTareSwitch.isChecked = false
            }
        }
    }

    private val stopwatch = buildStopwatch {
        // Setting the start format of the stopwatch
        startFormat("MM:SS:L")
        // Setting a tick listener that gets notified when time changes
        onTick { millis: Long, time: CharSequence ->
            timeString = time.toString()
            weightLog.add(weightRecord)
            if (weightLog.size % 10 == 0) {
                weightLogSecond.add(weightRecord)
                weightLogGraphData =
                    AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
                chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                    arrayOf(
                        weightLogGraphData
                    )
                )
            }
            if (weightLog.size < 2) {
                Log.d("INFO", "Waiting for weight data")
            } else if (weightLog.size <= 10) {
                val flowRateComputed = (weightLog.last() - weightLog.first())
                flowRate = if (flowRateComputed <= 0) {
                    0.0F
                } else {
                    flowRateComputed
                }
                flowRateLog.add(flowRate)
            } else {
                val flowRateComputed =
                    if ((weightLog.last() - weightLog[weightLog.size - 10]) <= 0) {
                        0.0F
                    } else {
                        (weightLog.last() - weightLog[weightLog.size - 10])
                    }
                flowRateLog.add(flowRateComputed)
                flowRate = flowRateLog.takeLast(10).average().toFloat()
            }
            if (doseRecord > 0) {
                val percentRatio = weightRecord / doseRecord
                brewRatioString = "1:${"%.1f".format(percentRatio)}"
            }
            if (flowRateLog.size > 0) {
                flowRateAvg = flowRateLog.average().toFloat()
            }
            if (flowRateLog.size % 10 == 0 && flowRateLog.size > 1) {
                flowRateLogSecond.add(flowRate)
                flowRateLogGraphData =
                    AASeriesElement().name("Flowrate").data(flowRateLogSecond.toTypedArray())
                chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                    arrayOf(
                        flowRateLogGraphData
                    )
                )
            }
        }
        useExactDelay(true)
    }

    private var isScanning = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isScanning) {
                    currentScanButtonText = "SCANNING"
                } else {
                    currentScanButtonText = "CONNECT"
                }
            }
        }

    private var isConnected = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isConnected) {
                    doseButton.isEnabled = true
                    resetButton.isEnabled = true
                    currentScanButtonText = "GO ON"
                } else {
                    doseButton.isEnabled = false
                    resetButton.isEnabled = false
                    currentScanButtonText = "CONNECT"
                }
            }
        }

    private var isTimerWorking = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isTimerWorking) {
                    currentScanButtonText = "PAUSE"
                    doseButton.isEnabled = false
                } else {
                    currentScanButtonText = "GO ON"
                    doseButton.isEnabled = true
                }
            }
        }

    private var isTimerPaused = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isTimerPaused) {
                    currentScanButtonText = "RESUME"
                } else {
                    currentScanButtonText = "PAUSE"
                }
            }
        }

    private var showFlowRateAvg = false
    private fun checkActiveConnection() {
        val checkList = central.getConnectedPeripherals()
        if (checkList.isEmpty()) {
            isConnected = false
            if (::scalePeripheral.isInitialized) {
                scope.launch { connectToScale(scalePeripheral) }
            }
        } else {
            isConnected = true
        }
    }

    private fun startStopWatch() {
        checkActiveConnection()
        isTimerWorking = true
        isTimerPaused = false
        if (weightLog.size == 0) {
            weightLog.add(weightRecord)
            weightLogSecond.add(weightRecord)
            weightLogGraphData =
                AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
            chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    weightLogGraphData
                )
            )
            Log.d("INFO", "Added weight ${weightRecord} to graph")
        }
        if (flowRateLog.size == 0) {
            flowRateLog.add(flowRate)
            flowRateLogSecond.add(flowRate)
            flowRateLogGraphData =
                AASeriesElement().name("Flowrate").data(flowRateLogSecond.toTypedArray())
            chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                arrayOf(
                    flowRateLogGraphData
                )
            )
            Log.d("INFO", "Added flowrate ${flowRate} to graph")
        }
        stopwatch.start()
    }

    private fun pauseStopWatch() {
        checkActiveConnection()
        isTimerPaused = true
        stopwatch.stop()
    }

    private fun resetStopWatch() {
        checkActiveConnection()
        isTimerPaused = false
        isTimerWorking = false
        stopwatch.reset()
    }

    private fun resetValues() {
        doseRecord = 0.0F
        weightRecord = 0.0F
        weightLog = mutableListOf<Float>()
        weightLogSecond = mutableListOf<Float>()
        weightLogGraphData = AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
        chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
            arrayOf(
                weightLogGraphData
            )
        )
        flowRate = 0.0F
        flowRateLog = mutableListOf<Float>()
        flowRateLogSecond = mutableListOf<Float>()
        flowRateLogGraphData =
            AASeriesElement().name("Flowrate").data(flowRateLogSecond.toTypedArray())
        chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
            arrayOf(
                flowRateLogGraphData
            )
        )
        flowRateAvg = 0.0F
        timeString = "00:00.0"
        brewRatioString = "1:0,0"
        checkActiveConnection()
    }

    private fun isSystemDarkMode(): Boolean {
        val configuration = Resources.getSystem().configuration
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        central = BluetoothCentralManager(requireActivity().applicationContext)
        // keep the fragment and all its data across screen rotation
        // setRetainInstance(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        if (mRootView == null) {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            mRootView = binding.root
        }

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

        autoStartSwitch = binding.autoStartSwitch
        autoTareSwitch = binding.autoTareSwitch
        autoDoseSwitch = binding.autoDoseSwitch

        chartWeightView = binding.chartWeightView
        chartFlowRateView = binding.chartFlowRateView

        chartWeightView.isClearBackgroundColor = true
        chartFlowRateView.isClearBackgroundColor = true

        chartWeightView.aa_drawChartWithChartModel(chartWeightViewModel)
        chartFlowRateView.aa_drawChartWithChartModel(chartFlowRateViewModel)

        chartFlowRateView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
            arrayOf(
                flowRateLogGraphData
            )
        )
        chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
            arrayOf(
                weightLogGraphData
            )
        )

        doseButton = binding.doseButton
        doseButton.setOnClickListener {
            if (isConnected) {
                scope.launch {
                    sendCommandToScale(
                        scalePeripheral, resetCommand
                    )
                }
            }
            doseRecord = weightRecord
        }

        resetButton = binding.resetButton
        resetButton.setOnClickListener {
            if (isConnected) {
                scope.launch {
                    sendCommandToScale(
                        scalePeripheral, resetCommand
                    )
                }
            }
            resetStopWatch()
            resetValues()
        }

        scanButton = binding.scanButton
        scanButton.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                if (isConnected) {
                    if (isTimerWorking) {
                        if (isTimerPaused) {
                            startStopWatch()
                        } else {
                            pauseStopWatch()
                        }
                    } else {
                        startStopWatch()
                    }
                } else {
                    startBleScan()
                }
            }
        }

        checkActiveConnection()

        if (savedInstanceState != null) {
            weightRecord = savedInstanceState.getFloat("weightRecord")
        }

        return mRootView as View
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG: String = "Weighting"
    }
}