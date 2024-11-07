package com.tomatishe.futulacoffeescale.coordinators.dataCoordinator

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataCoordinator {
    companion object {
        val shared = DataCoordinator()
        const val identifier = "[DataCoordinator]"
    }
    // MARK: Variables
    var context: Context? = null
    // Create a variable for each preference, along with a default value.
    // This is to guarantee that if it can't find it it resets to a value that you can control.
    var autoAll: Boolean = true
    val defaultAutoAll: Boolean = true
    var autoStart: Boolean = true
    val defaultAutoStart: Boolean = true
    var ignoreDose: Boolean = true
    val defaultIgnoreDose: Boolean = true
    var autoDose: Boolean = true
    val defaultAutoDose: Boolean = true
    var autoTare: Boolean = true
    val defaultAutoTare: Boolean = true
    var autoSwitches: Boolean = true
    val defaultAutoSwitches: Boolean = true
    var autoButtons: Boolean = false
    val defaultAutoButtons: Boolean = false
    var replaceResetWithTare: Boolean = false
    val defaultReplaceResetWithTare: Boolean = false
    var stopTimerWhenLostConnection: Boolean = true
    val defaultStopTimerWhenLostConnection: Boolean = true
    var startSearchAfterLaunch: Boolean = false
    val defaultStartSearchAfterLaunch: Boolean = false
    var oneGraphInHistory: Boolean = false
    val defaultOneGraphInHistory: Boolean = false
    var enableServerWeight: Boolean = false
    val defaultEnableServerWeight: Boolean = false
    var weightChartType: String = "AreaSpline"
    val defaultWeightChartType: String = "AreaSpline"
    var flowRateChartType: String = "AreaSpline"
    val defaultFlowRateChartType: String = "AreaSpline"

    // MARK: Data Store Variables
    private val USER_PREFERENCES_NAME = "fCoffeeScapePreferences"
    val Context.dataStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )

    // MARK: Lifecycle
    fun initialize(context: Context, onLoad: () -> Unit) {
        // Set Context
        this.context = context
        // Load DataStore Settings
        GlobalScope.launch(Dispatchers.Default) {
            // Update Sample Boolean
            autoAll = getAutoAll()
            autoStart = getAutoStart()
            ignoreDose = getIgnoreDose()
            autoDose = getAutoDose()
            autoTare = getAutoTare()
            autoSwitches = getAutoSwitches()
            autoButtons = getAutoButtons()
            replaceResetWithTare = getReplaceResetWithTare()
            stopTimerWhenLostConnection = getStopTimerWhenLostConnection()
            startSearchAfterLaunch = getStartSearchAfterLaunch()
            oneGraphInHistory = getOneGraphInHistory()
            enableServerWeight = getEnableServerWeight()
            weightChartType = getWeightChartType()
            flowRateChartType = getFlowRateChartType()
            // Callback
            onLoad()
        }
    }
}
