package com.tomatishe.futulacoffeescale.models.keys

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val autoAll = booleanPreferencesKey("autoAllSettings")
    val autoStart = booleanPreferencesKey("autoStartSettings")
    val ignoreDose = booleanPreferencesKey("ignoreDoseSettings")
    val autoDose = booleanPreferencesKey("autoDoseSettings")
    val autoTare = booleanPreferencesKey("autoTareSettings")
    val autoSwitches = booleanPreferencesKey("autoSwitchesSettings")
    val autoButtons = booleanPreferencesKey("autoButtonsSettings")
    val replaceResetWithTare = booleanPreferencesKey("replaceResetWithTare")
    val stopTimerWhenLostConnection = booleanPreferencesKey("stopTimerWhenLostConnection")
    val startSearchAfterLaunch = booleanPreferencesKey("startSearchAfterLaunch")
    val oneGraphInHistory = booleanPreferencesKey("oneGraphInHistory")
    val enableServerWeight = booleanPreferencesKey("enableServerWeight")
    val weightChartType = stringPreferencesKey("weightChartType")
    val flowRateChartType = stringPreferencesKey("flowRateChartType")
    val useFixedUnit = booleanPreferencesKey("useFixedUnit")
    val fixedUnit = stringPreferencesKey("fixedUnit")
    val alwaysConvertUnits = booleanPreferencesKey("alwaysConvertUnits")
    val preferredDisplayUnit = stringPreferencesKey("preferredDisplayUnit")
}
