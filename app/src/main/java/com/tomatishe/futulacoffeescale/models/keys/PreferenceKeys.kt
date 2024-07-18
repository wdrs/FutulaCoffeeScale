package com.tomatishe.futulacoffeescale.models.keys

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val autoAll = booleanPreferencesKey("autoAllSettings")
    val autoStart = booleanPreferencesKey("autoStartSettings")
    val ignoreDose = booleanPreferencesKey("ignoreDoseSettings")
    val autoDose = booleanPreferencesKey("autoDoseSettings")
    val autoTare = booleanPreferencesKey("autoTareSettings")
    val autoSwitches = booleanPreferencesKey("autoSwitchesSettings")
    val autoButtons = booleanPreferencesKey("autoButtonsSettings")
}
