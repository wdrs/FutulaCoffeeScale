package com.tomatishe.futulacoffeescale.ui.settings

import android.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.tomatishe.futulacoffeescale.Dependencies
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
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getOneGraphInHistory
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getReplaceResetWithTare
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getStartSearchAfterLaunch
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getStopTimerWhenLostConnection
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.getWeightChartType
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoAll
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoButtons
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoDose
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoStart
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoSwitches
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateAutoTare
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateEnableServerWeight
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateFlowRateChartType
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateIgnoreDose
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateOneGraphInHistory
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateReplaceResetWithTare
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateStartSearchAfterLaunch
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateStopTimerWhenLostConnection
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.updateWeightChartType
import com.tomatishe.futulacoffeescale.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var autoAllLayout: ConstraintLayout
    private lateinit var autoStartLayout: ConstraintLayout
    private lateinit var ignoreDoseLayout: ConstraintLayout
    private lateinit var autoDoseLayout: ConstraintLayout
    private lateinit var autoTareLayout: ConstraintLayout
    private lateinit var autoSwitchesLayout: ConstraintLayout
    private lateinit var autoButtonsLayout: ConstraintLayout

    private lateinit var autoFuncSettingsSwitch: MaterialSwitch
    private lateinit var autoStartSettingsSwitch: MaterialSwitch
    private lateinit var ignoreDoseSettingsSwitch: MaterialSwitch
    private lateinit var autoDoseSettingsSwitch: MaterialSwitch
    private lateinit var autoTareSettingsSwitch: MaterialSwitch
    private lateinit var autoHideSwitchesSettingsSwitch: MaterialSwitch
    private lateinit var autoHideButtonsSettingsSwitch: MaterialSwitch
    private lateinit var replaceResetWithTareSwitch: MaterialSwitch
    private lateinit var stopTimerWhenLostConnectionSwitch: MaterialSwitch
    private lateinit var startSearchAfterLaunchSwitch: MaterialSwitch
    private lateinit var showOneGraphInHistorySwitch: MaterialSwitch
    private lateinit var enableServerWeightSwitch: MaterialSwitch
    private lateinit var weightChartType: AppCompatAutoCompleteTextView
    private lateinit var flowRateChartType: AppCompatAutoCompleteTextView

    private val weightChartTypeArray = arrayOf("Area", "AreaSpline", "Column", "Line", "Spline")
    private val flowRateChartTypeArray = arrayOf("Area", "AreaSpline", "Column", "Line", "Spline")

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var autoFuncSettingsSwitchChecked = true
    private var autoStartSettingsSwitchChecked = true
    private var ignoreDoseSettingsSwitchChecked = false
    private var autoDoseSettingsSwitchChecked = true
    private var autoTareSettingsSwitchChecked = true
    private var autoHideSwitchesSettingsSwitchChecked = true
    private var autoHideButtonsSettingsSwitchChecked = false
    private var replaceResetWithTareSwitchChecked = false
    private var stopTimerWhenLostConnectionChecked = true
    private var startSearchAfterLaunchChecked = false
    private var oneGraphInHistory = false
    private var enableServerWeightChecked = false

    private var isSettingsLoaded = false
        set(value) {
            field = value
            if (isSettingsLoaded) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        autoFuncSettingsSwitch.isChecked = autoFuncSettingsSwitchChecked
                        autoStartSettingsSwitch.isChecked = autoStartSettingsSwitchChecked
                        ignoreDoseSettingsSwitch.isChecked = ignoreDoseSettingsSwitchChecked
                        autoDoseSettingsSwitch.isChecked = autoDoseSettingsSwitchChecked
                        autoTareSettingsSwitch.isChecked = autoTareSettingsSwitchChecked
                        autoHideSwitchesSettingsSwitch.isChecked =
                            autoHideSwitchesSettingsSwitchChecked
                        autoHideButtonsSettingsSwitch.isChecked =
                            autoHideButtonsSettingsSwitchChecked
                        replaceResetWithTareSwitch.isChecked = replaceResetWithTareSwitchChecked
                        stopTimerWhenLostConnectionSwitch.isChecked =
                            stopTimerWhenLostConnectionChecked
                        startSearchAfterLaunchSwitch.isChecked = startSearchAfterLaunchChecked
                        showOneGraphInHistorySwitch.isChecked = oneGraphInHistory
                        enableServerWeightSwitch.isChecked = enableServerWeightChecked

                        if (autoStartSettingsSwitch.isChecked) {
                            ignoreDoseLayout.visibility = View.VISIBLE
                        } else {
                            ignoreDoseLayout.visibility = View.GONE
                        }

                        if (autoFuncSettingsSwitch.isChecked) {
                            autoStartLayout.visibility = View.VISIBLE
                            if (autoStartSettingsSwitch.isChecked) {
                                ignoreDoseLayout.visibility = View.VISIBLE
                            }
                            autoDoseLayout.visibility = View.VISIBLE
                            autoTareLayout.visibility = View.VISIBLE
                        } else {
                            autoStartLayout.visibility = View.GONE
                            ignoreDoseLayout.visibility = View.GONE
                            autoDoseLayout.visibility = View.GONE
                            autoTareLayout.visibility = View.GONE
                        }
                    },
                    100
                )
            }
        }

    private suspend fun getChartTypes() {
        val wcType = DataCoordinator.shared.getWeightChartType()
        val frType = DataCoordinator.shared.getFlowRateChartType()
        Handler(Looper.getMainLooper()).post {
            weightChartType.setText(wcType, false)
            flowRateChartType.setText(frType, false)
        }
        weightChartType.doOnTextChanged { text, start, before, count ->
            DataCoordinator.shared.updateWeightChartType(
                text.toString()
            )
        }
        flowRateChartType.doOnTextChanged { text, start, before, count ->
            DataCoordinator.shared.updateFlowRateChartType(
                text.toString()
            )
        }
    }

    override fun onStart() {
        isSettingsLoaded = false
        super.onStart()
        activity?.runOnUiThread {
            scope.launch {
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
                oneGraphInHistory = DataCoordinator.shared.getOneGraphInHistory()
                enableServerWeightChecked = DataCoordinator.shared.getEnableServerWeight()
                isSettingsLoaded = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        autoAllLayout = binding.autoAllLayout
        autoStartLayout = binding.autoStartLayout
        ignoreDoseLayout = binding.ignoreDoseLayout
        autoDoseLayout = binding.autoDoseLayout
        autoTareLayout = binding.autoTareLayout
        autoSwitchesLayout = binding.autoSwitchesLayout
        autoButtonsLayout = binding.autoButtonsLayout

        autoFuncSettingsSwitch = binding.autoFuncSettingsSwitch
        autoStartSettingsSwitch = binding.autoStartSettingsSwitch
        ignoreDoseSettingsSwitch = binding.ignoreDoseSettingsSwitch
        autoDoseSettingsSwitch = binding.autoDoseSettingsSwitch
        autoTareSettingsSwitch = binding.autoTareSettingsSwitch
        autoHideSwitchesSettingsSwitch = binding.autoHideSwitchesSettingsSwitch
        autoHideButtonsSettingsSwitch = binding.autoHideButtonsSettingsSwitch
        replaceResetWithTareSwitch = binding.replaceResetWithTareSwitch
        stopTimerWhenLostConnectionSwitch = binding.stopTimerWhenLostConnectionSwitch
        startSearchAfterLaunchSwitch = binding.startSearchAfterLaunchSwitch
        showOneGraphInHistorySwitch = binding.showOneGraphInHistorySwitch
        enableServerWeightSwitch = binding.enableServerWeightSwitch
        weightChartType = binding.selectWeightGraphTypeAuto
        flowRateChartType = binding.selectFlowRateGraphTypeAuto

        weightChartType.setAdapter(
            ArrayAdapter(
                binding.root.context,
                R.layout.simple_list_item_1,
                weightChartTypeArray.toMutableList()
            )
        )

        flowRateChartType.setAdapter(
            ArrayAdapter(
                binding.root.context,
                R.layout.simple_list_item_1,
                flowRateChartTypeArray.toMutableList()
            )
        )

        weightChartType.isFocusable = false
        weightChartType.isCursorVisible = false
        weightChartType.keyListener = null

        flowRateChartType.isFocusable = false
        flowRateChartType.isCursorVisible = false
        flowRateChartType.keyListener = null

        GlobalScope.launch {
            getChartTypes()
        }

        autoFuncSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoAll(autoFuncSettingsSwitch.isChecked)
            if (autoFuncSettingsSwitch.isChecked) {
                autoStartLayout.visibility = View.VISIBLE
                if (autoStartSettingsSwitch.isChecked) {
                    ignoreDoseLayout.visibility = View.VISIBLE
                }
                autoDoseLayout.visibility = View.VISIBLE
                autoTareLayout.visibility = View.VISIBLE
            } else {
                autoStartLayout.visibility = View.GONE
                ignoreDoseLayout.visibility = View.GONE
                autoDoseLayout.visibility = View.GONE
                autoTareLayout.visibility = View.GONE
            }
        }

        autoStartSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoStart(autoStartSettingsSwitch.isChecked)
            if (autoStartSettingsSwitch.isChecked) {
                ignoreDoseLayout.visibility = View.VISIBLE
            } else {
                ignoreDoseLayout.visibility = View.GONE
                if (!autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                    autoFuncSettingsSwitch.performClick()
                }
            }
        }

        ignoreDoseSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateIgnoreDose(ignoreDoseSettingsSwitch.isChecked)
        }

        autoDoseSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoDose(autoDoseSettingsSwitch.isChecked)
            if (!autoStartSettingsSwitch.isChecked && !autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                autoFuncSettingsSwitch.performClick()
            }
        }

        autoTareSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoTare(autoTareSettingsSwitch.isChecked)
            if (!autoStartSettingsSwitch.isChecked && !autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                autoFuncSettingsSwitch.performClick()
            }
        }

        autoHideSwitchesSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoSwitches(autoHideSwitchesSettingsSwitch.isChecked)
        }

        autoHideButtonsSettingsSwitch.setOnClickListener {
            DataCoordinator.shared.updateAutoButtons(autoHideButtonsSettingsSwitch.isChecked)
        }

        replaceResetWithTareSwitch.setOnClickListener {
            DataCoordinator.shared.updateReplaceResetWithTare(replaceResetWithTareSwitch.isChecked)
        }

        stopTimerWhenLostConnectionSwitch.setOnClickListener {
            DataCoordinator.shared.updateStopTimerWhenLostConnection(
                stopTimerWhenLostConnectionSwitch.isChecked
            )
        }

        startSearchAfterLaunchSwitch.setOnClickListener {
            DataCoordinator.shared.updateStartSearchAfterLaunch(
                startSearchAfterLaunchSwitch.isChecked
            )
        }

        showOneGraphInHistorySwitch.setOnClickListener {
            DataCoordinator.shared.updateOneGraphInHistory(
                showOneGraphInHistorySwitch.isChecked
            )
        }

        enableServerWeightSwitch.setOnClickListener {
            DataCoordinator.shared.updateEnableServerWeight(
                enableServerWeightSwitch.isChecked
            )
        }

        //val textView: TextView = binding.textSlideshow
        //settingsViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}