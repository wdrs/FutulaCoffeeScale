package com.tomatishe.futulacoffeescale.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.withTransaction
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.tomatishe.futulacoffeescale.Dependencies
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.WeightRecord
import com.tomatishe.futulacoffeescale.WeightRecordExtra
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.DataCoordinator
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoAll
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoButtons
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoDose
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoStart
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoSwitches
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getAutoTare
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getEnableServerWeight
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getFlowRateChartType
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getIgnoreDose
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getReplaceResetWithTare
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getStartSearchAfterLaunch
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getStopTimerWhenLostConnection
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getWeightChartType
import com.tomatishe.futulacoffeescale.databinding.FragmentHomeBinding
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ConnectionFailedException
import com.welie.blessed.ConnectionState
import com.welie.blessed.GattException
import com.welie.blessed.WriteType
import com.welie.blessed.asUInt8
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timerx.buildStopwatch
import timerx.buildTimer
import java.util.Calendar
import java.util.Date
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
/* private const val deviceNameUuidString = "00002a00-0000-1000-8000-00805f9b34fb"
private const val deviceManufacturerNameUuidString = "00002a29-0000-1000-8000-00805f9b34fb"
private const val deviceSerialNumberUuidString = "00002a25-0000-1000-8000-00805f9b34fb"
private const val deviceSoftwareRevisionUuidString = "00002a28-0000-1000-8000-00805f9b34fb"
private const val deviceFirmwareRevisionUuidString = "00002a26-0000-1000-8000-00805f9b34fb"
private const val deviceSystemIDUuidString = "00002a23-0000-1000-8000-00805f9b34fb" */

class HomeFragment : Fragment() {

    abstract class DoubleClickListener : View.OnClickListener {
        var lastClickTime: Long = 0

        override fun onClick(v: View?) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v)
            }
            lastClickTime = clickTime
        }

        abstract fun onDoubleClick(v: View?)

        companion object {
            //milliseconds
            private const val DOUBLE_CLICK_TIME_DELTA: Long = 300
        }
    }

    private fun String.replaceLast(oldValue: String, newValue: String): String {
        val lastIndex = lastIndexOf(oldValue)
        if (lastIndex == -1) {
            return this
        }
        val prefix = substring(0, lastIndex)
        val suffix = substring(lastIndex + oldValue.length)
        return "$prefix$newValue$suffix"
    }

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

    private var weightChartType: String = "AreaSpline"
    private var flowRateChartType: String = "AreaSpline"

    private var mRootView: View? = null
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var batteryLevel: Int = 100
    private var doseRecord: Float = 0.0F
        set(value) {
            field = value
            activity?.runOnUiThread {
                doseText.text = "%.1f".format(doseRecord)
            }
        }

    private var weightRecordLocked: Float = 0.0F
    private var weightRecord: Float = 0.0F
        set(value) {
            field = value
            activity?.runOnUiThread {
                waitWatch.reset()
                waitWatch.start()
                weightText.text = "%.1f".format(weightRecord)
                if (autoFuncSettingsSwitchChecked && autoStartSettingsSwitchChecked) {
                    if (
                        autoStartSwitch.isChecked &&
                        (if (autoDoseSettingsSwitchChecked) {
                            !autoDoseSwitch.isChecked
                        } else true) &&
                        (if (autoTareSettingsSwitchChecked) {
                            !autoTareSwitch.isChecked
                        } else true) &&
                        (doseRecord > 0 || ignoreDoseSettingsSwitchChecked) &&
                        !isTimerWorking &&
                        weightRecord > 0.1
                    ) {
                        startStopWatch()
                    }
                }
            }
        }
    private var weightUnit: String = "g"
    private var weightLog = mutableListOf<Float>()
    private var weightLogSecond = mutableListOf<Float>()
    private var weightLogGraphData =
        AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
    private var serverWeight: Float = 0.0F
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
                if (timeString.count { it == ':' } == 2) {
                    timeText.text = timeString.replaceLast(":", ".")
                } else {
                    timeText.text = timeString
                }
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

    private lateinit var chartWeightViewModel: AAChartModel
    private lateinit var chartFlowRateViewModel: AAChartModel

    private var currentScanButtonText: String = ""
        set(value) {
            field = value
            activity?.runOnUiThread {
                scanButton.text = currentScanButtonText
            }
        }
    private var currentDoseButtonText: String = ""
        set(value) {
            field = value
            activity?.runOnUiThread {
                doseButton.text = currentDoseButtonText
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
    private lateinit var switchesLayout: ConstraintLayout
    private lateinit var buttonsLayout: ConstraintLayout
    private lateinit var chartWeightView: AAChartView
    private lateinit var chartFlowRateView: AAChartView
    private lateinit var scalePeripheral: BluetoothPeripheral
    private lateinit var central: BluetoothCentralManager

    private lateinit var weightCharacteristic: BluetoothGattCharacteristic
    private lateinit var batteryCharacteristic: BluetoothGattCharacteristic

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
                connectToScale(scalePeripheral)
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
            Log.d("ERROR", "Connection to ${peripheral.name} failed while sending command")
            isConnected = false
            isScanning = false
            currentScanButtonText = getString(R.string.button_connect)
        }
    }

    private fun handlePeripheral(peripheral: BluetoothPeripheral) {
        GlobalScope.launch {
            try {
                weightCharacteristic = peripheral.getCharacteristic(
                    UUID.fromString(scaleServiceUuidString), UUID.fromString(
                        scaleGetWeightUuidString
                    )
                )!!
                batteryCharacteristic = peripheral.getCharacteristic(
                    UUID.fromString(scaleBatteryServiceUuidString), UUID.fromString(
                        scaleBatteryLevelUuidString
                    )
                )!!

                peripheral.observe(weightCharacteristic) { value ->
                    try {
                        val weightSign =
                            if (value.sliceArray(5..5)[0].toInt() > 0) -1.0 else 1.0
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
                        if (isWeightLocked) {
                            weightRecord = weightRecordLocked
                        } else {
                            weightRecord =
                                (intFrom2ByteArray(value.sliceArray(3..4)).toFloat() / 10) * weightSign.toFloat() * weightUnitMultiplier.toFloat()
                        }
                    } catch (exception: Exception) {
                        Log.d(
                            "ERROR",
                            "Connection to ${peripheral.name} failed while observing weight"
                        )
                        isConnected = false
                        isScanning = false
                        if (isTimerWorking && !isTimerPaused && stopTimerWhenLostConnectionChecked) {
                            stopwatch.stop()
                            isTimerPaused = true
                        }
                        currentScanButtonText = getString(R.string.button_connect)
                    }
                }

                peripheral.observe(batteryCharacteristic) { value ->
                    try {
                        batteryLevel = value.asUInt8()?.toInt()!!
                        Log.d("INFO", "BATTERY = ${batteryLevel}")
                    } catch (exception: Exception) {
                        Log.d(
                            "ERROR",
                            "Connection to ${peripheral.name} failed while observing battery"
                        )
                        isConnected = false
                        isScanning = false
                        if (isTimerWorking && !isTimerPaused && stopTimerWhenLostConnectionChecked) {
                            stopwatch.stop()
                            isTimerPaused = true
                        }
                        currentScanButtonText = getString(R.string.button_connect)
                    }
                }

                sendCommandToScale(peripheral, unitGramCommand)

            } catch (e: IllegalArgumentException) {
                Log.d("ERROR", "$e")
            } catch (b: GattException) {
                Log.d("ERROR", "$b")
            } catch (n: NullPointerException) {
                Log.d("ERROR", "$n")
            }
        }
    }

    private fun connectToScale(peripheral: BluetoothPeripheral) {
        currentScanButtonText = getString(R.string.button_connect_connecting)
        GlobalScope.launch {
            try {
                central.connectPeripheral(peripheral)
            } catch (connectionFailed: ConnectionFailedException) {
                Log.d("ERROR", "Connection Failed")
            }
        }
        central.observeConnectionState { peripheralCurr, state ->
            Log.d("INFO", "Peripheral ${peripheralCurr.name} has $state")
            when (state) {
                ConnectionState.CONNECTING -> currentScanButtonText =
                    getString(R.string.button_connect_connecting)

                ConnectionState.CONNECTED -> {
                    isConnected = true
                    handlePeripheral(peripheral)
                }

                ConnectionState.DISCONNECTING -> {
                    currentScanButtonText = getString(R.string.button_connect_disconnecting)
                }

                ConnectionState.DISCONNECTED -> {
                    isConnected = false

                    currentScanButtonText = getString(R.string.button_connect)

                    if (isTimerWorking && !isTimerPaused && stopTimerWhenLostConnectionChecked) {
                        stopwatch.stop()
                        isTimerPaused = true
                    }

                    GlobalScope.launch {
                        delay(10000)
                        if (central.getPeripheral(peripheral.address)
                                .getState() == ConnectionState.DISCONNECTED
                        ) {
                            currentScanButtonText = getString(R.string.button_connect_connecting)
                            try {
                                central.connectPeripheral(peripheral)
                            } catch (connectionFailed: ConnectionFailedException) {
                                Log.d("ERROR", "Connection Failed")
                                currentScanButtonText = getString(R.string.button_connect)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        central.stopScan()
        isScanning = false
    }

    private val scanFilter = ScanFilter.Builder().setDeviceName(SCALE_NAME).build()

    private val waitWatch = buildTimer {
        startFormat("MM:SS")
        useExactDelay(true)
        startTime(2, TimeUnit.SECONDS)
        actionWhen(1, TimeUnit.SECONDS) {
            if (autoFuncSettingsSwitchChecked) {
                if (autoDoseSwitch.isChecked && !autoTareSwitch.isChecked && !isTimerWorking && weightRecord > 0.2) {
                    doseRecord = weightRecord
                    GlobalScope.launch {
                        sendCommandToScale(
                            scalePeripheral, resetCommand
                        )
                    }
                    autoDoseSwitch.isChecked = false
                }
                if (autoTareSwitch.isChecked && !isTimerWorking && weightRecord > 0.2) {
                    weightRecord = 0.0F
                    GlobalScope.launch {
                        sendCommandToScale(
                            scalePeripheral, resetCommand
                        )
                    }
                    autoTareSwitch.isChecked = false
                }
            }
        }
    }

    private val stopwatch = buildStopwatch {
        // Setting the start format of the stopwatch
        startFormat("MM:SS:L")
        // Setting a tick listener that gets notified when time changes
        onTick { millis: Long, time: CharSequence ->
            timeString = time.toString()
            /* val preserveWeight = weightLog.last()
            if (weightLog.size > 1) {
                Log.d("INFO", "CURRENT ${weightRecord} LAST ${preserveWeight}")
                if (weightRecord < preserveWeight) {
                    weightRecord = preserveWeight
                }
            } */
            val weightRecordComputed = if (weightRecord < 0) {
                0.0F
            } else {
                weightRecord
            }

            if (weightLog.size < 2) {
                Log.d("INFO", "Waiting for weight data")
            } else if (weightLog.size <= 10) {
                // val flowRateComputed = (weightLog.last() - weightLog.first())
                val flowRateComputed = (weightRecordComputed - weightLog.first())
                flowRate = if (flowRateComputed <= 0) {
                    0.0F
                } else {
                    flowRateComputed
                }
                flowRateLog.add(flowRate)
            } else if (weightLog.size > 10) {
                /* val flowRateComputed =
                if ((weightLog.last() - weightLog[weightLog.size - 10]) <= 0) {
                    0.0F
                } else {
                    (weightLog.last() - weightLog[weightLog.size - 10])
                } */
                val flowRateComputed =
                    if ((weightRecordComputed - weightLog.subList(0, weightLog.size - 10)
                            .max()) <= 0
                    ) {
                        0.0F
                    } else {
                        (weightRecordComputed - weightLog.subList(0, weightLog.size - 10).max())
                    }
                flowRateLog.add(flowRateComputed)
                flowRate = flowRateLog.takeLast(10).average().toFloat()
            }

            if (doseRecord > 0) {
                val percentRatio = weightRecordComputed / doseRecord
                brewRatioString = "1:${"%.1f".format(percentRatio)}"
            }

            if (flowRateLog.size > 0) {
                val flt = flowRateLog.toMutableList()
                flt.retainAll { it >= 0.1F }
                flowRateAvg = if (flt.size > 0) {
                    flt.average().toFloat()
                } else {
                    0.0F
                }
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

            weightLog.add(weightRecordComputed)
            if (weightLog.size % 10 == 0) {
                weightLogSecond.add(weightRecordComputed)
                weightLogGraphData =
                    AASeriesElement().name("Weight").data(weightLogSecond.toTypedArray())
                chartWeightView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(
                    arrayOf(
                        weightLogGraphData
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
                    currentScanButtonText = getString(R.string.button_connect_scanning)
                } else {
                    currentScanButtonText = getString(R.string.button_connect)
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
                    currentScanButtonText = getString(R.string.button_connect_start_brew)
                } else {
                    if (!isTimerWorking && !stopTimerWhenLostConnectionChecked) {
                        doseButton.isEnabled = false
                    }
                    resetButton.isEnabled = false
                    currentScanButtonText = getString(R.string.button_connect)
                }
            }
        }

    private var isTimerWorking = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isTimerWorking) {
                    currentScanButtonText = getString(R.string.button_connect_pause_brew)
                    currentDoseButtonText = getString(R.string.button_dose_finish)
                    isDoseBecameFinish = true
                    if (autoFuncSettingsSwitchChecked && autoHideSwitchesSettingsSwitchChecked) {
                        switchesLayout.visibility = View.GONE
                    }
                } else {
                    currentScanButtonText = getString(R.string.button_connect_start_brew)
                    if (autoFuncSettingsSwitchChecked && autoHideSwitchesSettingsSwitchChecked) {
                        switchesLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

    private var isTimerPaused = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isTimerPaused) {
                    currentScanButtonText = getString(R.string.button_connect_resume_brew)
                    if (isTimerWorking) {
                        currentDoseButtonText = getString(R.string.button_dose_finish)
                        isDoseBecameFinish = true
                    } else {
                        doseButton.isEnabled = true
                    }
                } else {
                    currentScanButtonText = getString(R.string.button_connect_pause_brew)
                    if (isTimerWorking) {
                        currentDoseButtonText = getString(R.string.button_dose_finish)
                        isDoseBecameFinish = true
                    } else {
                        if (isConnected) {
                            doseButton.isEnabled = true
                        }
                    }
                }
            }
        }

    private var showFlowRateAvg = false
    private var isDoseBecameFinish = false
    private var isWeightLocked = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (isWeightLocked) {
                    weightLabel.text = getString(R.string.weight_text_locked)
                    weightRecordLocked = weightRecord
                } else {
                    weightLabel.text = getString(R.string.weight_text)
                    weightRecordLocked = 0.0F
                }
            }
        }

    private var autoFuncSettingsSwitchChecked = true
    private var autoStartSettingsSwitchChecked = true
    private var ignoreDoseSettingsSwitchChecked = true
    private var autoDoseSettingsSwitchChecked = true
    private var autoTareSettingsSwitchChecked = true
    private var autoHideSwitchesSettingsSwitchChecked = true
    private var autoHideButtonsSettingsSwitchChecked = false
    private var replaceResetWithTareSwitchChecked = false
    private var stopTimerWhenLostConnectionChecked = true
    private var startSearchAfterLaunchChecked = false
        set(value) {
            field = value
            if (startSearchAfterLaunchChecked && !isConnected) {
                startBleScan()
            }
        }
    private var enableServerWeightChecked = false

    private var isSettingsLoaded = false
        set(value) {
            field = value
            if (isSettingsLoaded) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (autoTareSettingsSwitchChecked || autoDoseSettingsSwitchChecked || autoStartSettingsSwitchChecked) {
                            switchesLayout.visibility = View.VISIBLE
                        }

                        if (replaceResetWithTareSwitchChecked) {
                            resetButton.text = getString(R.string.button_reset_as_tare)
                        } else {
                            resetButton.text = getString(R.string.button_reset)
                        }

                        if (autoTareSettingsSwitchChecked) {
                            autoTareSwitch.visibility = View.VISIBLE
                        } else {
                            autoTareSwitch.visibility = View.GONE
                        }

                        if (autoDoseSettingsSwitchChecked) {
                            autoDoseSwitch.visibility = View.VISIBLE
                        } else {
                            autoDoseSwitch.visibility = View.GONE
                        }

                        if (autoStartSettingsSwitchChecked) {
                            autoStartSwitch.visibility = View.VISIBLE
                        } else {
                            autoStartSwitch.visibility = View.GONE
                        }

                        if (autoFuncSettingsSwitchChecked) {
                            switchesLayout.visibility = View.VISIBLE
                        } else {
                            switchesLayout.visibility = View.GONE
                        }

                        if (isTimerWorking && autoHideSwitchesSettingsSwitchChecked && switchesLayout.visibility == View.VISIBLE) {
                            switchesLayout.visibility = View.GONE
                        }

                        chartWeightViewModel = AAChartModel()
                            .chartType(returnChartType(weightChartType))
                            .yAxisTitle(getString(R.string.weight_text)).markerRadius(0)
                            .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false)
                            .legendEnabled(false)
                            .dataLabelsEnabled(false).series(arrayOf(weightLogGraphData))
                            .categories(chartViewCategories)
                            .colorsTheme(arrayOf(primaryChartColor))
                            .backgroundColor("#00000000")

                        chartFlowRateViewModel = AAChartModel()
                            .chartType(returnChartType(flowRateChartType))
                            .yAxisTitle(getString(R.string.flowrate_text)).markerRadius(0)
                            .animationType(AAChartAnimationType.Elastic).tooltipEnabled(false)
                            .legendEnabled(false)
                            .dataLabelsEnabled(false).series(arrayOf(flowRateLogGraphData))
                            .categories(chartViewCategories)
                            .colorsTheme(arrayOf(primaryChartColor))
                            .backgroundColor("#00000000")

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
                    }, 100
                )
            }
        }

    private fun startStopWatch() {
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
        isTimerPaused = true
        stopwatch.stop()
    }

    private fun resetStopWatch() {
        isTimerPaused = false
        isTimerWorking = false
        stopwatch.reset()
    }

    private fun resetValues() {
        doseRecord = 0.0F
        weightRecord = 0.0F
        weightRecordLocked = 0.0F
        isWeightLocked = false
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
        currentDoseButtonText = getString(R.string.button_dose)
        isDoseBecameFinish = false
        if (autoHideButtonsSettingsSwitchChecked) {
            buttonsLayout.visibility = View.VISIBLE
        }
        if (isConnected) {
            doseButton.isEnabled = true
        }
        scanButton.isEnabled = true
        if (!isConnected) {
            Log.d("INFO", "${weightLog.size}")
            if (weightLog.size > 0) {
                doseButton.isEnabled = true
            }
            currentScanButtonText = getString(R.string.button_connect)
        }
    }

    private fun isSystemDarkMode(): Boolean {
        val configuration = Resources.getSystem().configuration
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private suspend fun saveCurrentBrew() {
        val brewDateTime: Date = Calendar.getInstance().time
        val brewDateTimeAsLong: Long = brewDateTime.time
        val weightLogString = weightLogSecond.joinToString(
            separator = ";"
        )
        val flowRateLogString = flowRateLogSecond.joinToString(
            separator = ";"
        )
        val newWeightRecord = WeightRecord(
            brewDateTimeAsLong,
            weightUnit,
            doseRecord,
            weightRecord,
            weightLogString,
            flowRate,
            flowRateAvg,
            flowRateLogString,
            timeString,
            brewRatioString
        )
        if (weightLogSecond.size > 0) {
            GlobalScope.launch {
                Dependencies.appDatabase.withTransaction {
                    val newId =
                        Dependencies.weightRecordRepository.insertWeightRecordData(newWeightRecord.toWeightRecordDbEntity())
                    val newWeightRecordExtra = WeightRecordExtra(
                        weightId = newId,
                        coffeeBean = null,
                        coffeeGrinder = null,
                        coffeeGrinderLevel = null,
                        gadgetName = null,
                        waterTemp = null,
                        extraInfo = null,
                        coffeeRoaster = null,
                    )
                    Dependencies.weightRecordRepositoryExtra.insertWeightRecordExtraData(
                        newWeightRecordExtra.toWeightRecordDbEntityExtra()
                    )
                }
            }
            Snackbar.make(
                mRootView!!, R.string.action_saved, Snackbar.LENGTH_SHORT
            ).setAnchorView(R.id.switchesLayout).show()
        } else {
            Snackbar.make(
                mRootView!!, R.string.action_not_saved_empty, Snackbar.LENGTH_SHORT
            ).setAnchorView(R.id.switchesLayout).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        central = BluetoothCentralManager(requireContext())
        // keep the fragment and all its data across screen rotation
        // setRetainInstance(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        GlobalScope.launch {
            enableServerWeightChecked = DataCoordinator.shared.getEnableServerWeight()
            Handler(Looper.getMainLooper()).post {
                menu.findItem(R.id.action_server).isVisible = enableServerWeightChecked
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_server -> {
                if (serverWeight == 0.0F) {
                    if (weightRecord > 0) {
                        serverWeight = weightRecord
                        Handler(Looper.getMainLooper()).post {
                            item.setIcon(R.drawable.baseline_filter_alt_24)
                        }
                    }
                } else {
                    weightRecord += doseRecord + serverWeight
                    isWeightLocked = true
                    serverWeight = 0.0F
                    if (weightRecord > 0 && doseRecord > 0) {
                        val percentRatio = weightRecord / doseRecord
                        brewRatioString = "1:${"%.1f".format(percentRatio)}"
                    }
                    Handler(Looper.getMainLooper()).post {
                        item.setIcon(R.drawable.outline_filter_alt_24)
                    }
                }
                true
            }

            R.id.action_save -> {
                GlobalScope.launch {
                    saveCurrentBrew()
                }
                true
            }

            R.id.action_reset -> {
                if (isConnected) {
                    GlobalScope.launch {
                        sendCommandToScale(
                            scalePeripheral, resetCommand
                        )
                    }
                }
                resetStopWatch()
                resetValues()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
                flowRateLabel.text = getString(R.string.flowrate_text)
                flowRateText.text = "%.1f".format(flowRate)
            } else {
                showFlowRateAvg = true
                flowRateLabel.text = getString(R.string.flowrate_avg_text)
                flowRateText.text = "%.1f".format(flowRateAvg)
            }
        }

        weightLabel.setOnClickListener {
            isWeightLocked = !isWeightLocked
        }

        weightText.setOnClickListener {
            isWeightLocked = !isWeightLocked
        }

        timeText = binding.timeText
        brewRatioText = binding.brewRatioText

        autoStartSwitch = binding.autoStartSwitch
        autoTareSwitch = binding.autoTareSwitch
        autoDoseSwitch = binding.autoDoseSwitch

        switchesLayout = binding.switchesLayout
        buttonsLayout = binding.buttonsLayout

        chartWeightView = binding.chartWeightView
        chartFlowRateView = binding.chartFlowRateView


        doseButton = binding.doseButton
        doseButton.setOnClickListener {
            if (isDoseBecameFinish) {
                pauseStopWatch()
                GlobalScope.launch {
                    saveCurrentBrew()
                }
                scanButton.isEnabled = false
                doseButton.isEnabled = false
                if (autoHideButtonsSettingsSwitchChecked) {
                    buttonsLayout.visibility = View.GONE
                }
            } else {
                if (isConnected) {
                    GlobalScope.launch {
                        sendCommandToScale(
                            scalePeripheral, resetCommand
                        )
                    }
                }
                doseRecord = weightRecord
            }
        }

        resetButton = binding.resetButton
        resetButton.setOnClickListener {
            if (isConnected) {
                GlobalScope.launch {
                    sendCommandToScale(
                        scalePeripheral, resetCommand
                    )
                }
            }
            if (!replaceResetWithTareSwitchChecked) {
                resetStopWatch()
                resetValues()
            }
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

        if (savedInstanceState != null) {
            weightRecord = savedInstanceState.getFloat("weightRecord")
        }

        if (isDoseBecameFinish && !scanButton.isEnabled) {
            doseButton.isEnabled = false
        }

        val manualDoseTextField = EditText(requireActivity())
        manualDoseTextField.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        manualDoseTextField.hint = getString(R.string.dose_edit_hint)

        val manualDoseDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dose_edit_title))
            //.setMessage("Message")
            .setView(manualDoseTextField)
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                doseRecord = manualDoseTextField.text.toString().toFloat()
                if (autoDoseSettingsSwitchChecked && autoDoseSwitch.isChecked) {
                    autoDoseSwitch.isChecked = false
                }
                if (weightLog.size > 0 && weightLog.last() > 0 && doseRecord > 0) {
                    val percentRatio = weightLog.last() / doseRecord
                    brewRatioString = "1:${"%.1f".format(percentRatio)}"
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create()

        val computedMargin =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
                .toInt()

        doseText.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                manualDoseDialog.show()
                manualDoseTextField.updateLayoutParams<FrameLayout.LayoutParams> {
                    this.leftMargin = computedMargin
                    this.rightMargin = computedMargin
                }
            }
        })
        doseLabel.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                manualDoseDialog.show()
                manualDoseTextField.updateLayoutParams<FrameLayout.LayoutParams> {
                    this.leftMargin = computedMargin
                    this.rightMargin = computedMargin
                }
            }
        })

        activity?.runOnUiThread {
            GlobalScope.launch {
                autoFuncSettingsSwitchChecked = DataCoordinator.shared.getAutoAll()
                autoStartSettingsSwitchChecked = DataCoordinator.shared.getAutoStart()
                ignoreDoseSettingsSwitchChecked = DataCoordinator.shared.getIgnoreDose()
                autoDoseSettingsSwitchChecked = DataCoordinator.shared.getAutoDose()
                autoTareSettingsSwitchChecked = DataCoordinator.shared.getAutoTare()
                autoHideSwitchesSettingsSwitchChecked = DataCoordinator.shared.getAutoSwitches()
                autoHideButtonsSettingsSwitchChecked = DataCoordinator.shared.getAutoButtons()
                replaceResetWithTareSwitchChecked = DataCoordinator.shared.getReplaceResetWithTare()
                stopTimerWhenLostConnectionChecked =
                    DataCoordinator.shared.getStopTimerWhenLostConnection()
                startSearchAfterLaunchChecked = DataCoordinator.shared.getStartSearchAfterLaunch()
                weightChartType = DataCoordinator.shared.getWeightChartType()
                flowRateChartType = DataCoordinator.shared.getFlowRateChartType()
                isSettingsLoaded = true
            }

        }

        return mRootView as View
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG: String = "Weighting"
    }
}