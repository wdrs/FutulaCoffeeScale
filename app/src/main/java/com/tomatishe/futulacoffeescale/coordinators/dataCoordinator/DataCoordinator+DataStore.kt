package com.tomatishe.futulacoffeescale.coordinators.dataCoordinator

import androidx.datastore.preferences.core.edit
import com.tomatishe.futulacoffeescale.models.keys.PreferencesKeys
import kotlinx.coroutines.flow.firstOrNull

suspend fun DataCoordinator.getAutoAll(): Boolean {
    val context = this.context ?: return defaultAutoAll
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoAll)
        ?: defaultAutoAll
}

suspend fun DataCoordinator.setAutoAll(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoAll] = value
    }
}

// ---

suspend fun DataCoordinator.getAutoStart(): Boolean {
    val context = this.context ?: return defaultAutoStart
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoStart)
        ?: defaultAutoStart
}

suspend fun DataCoordinator.setAutoStart(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoStart] = value
    }
}

// ---

suspend fun DataCoordinator.getIgnoreDose(): Boolean {
    val context = this.context ?: return defaultIgnoreDose
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.ignoreDose)
        ?: defaultIgnoreDose
}

suspend fun DataCoordinator.setIgnoreDose(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.ignoreDose] = value
    }
}

// ---

suspend fun DataCoordinator.getAutoDose(): Boolean {
    val context = this.context ?: return defaultAutoDose
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoDose)
        ?: defaultAutoDose
}

suspend fun DataCoordinator.setAutoDose(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoDose] = value
    }
}

// ---

suspend fun DataCoordinator.getAutoTare(): Boolean {
    val context = this.context ?: return defaultAutoTare
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoTare)
        ?: defaultAutoTare
}

suspend fun DataCoordinator.setAutoTare(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoTare] = value
    }
}

// ---

suspend fun DataCoordinator.getAutoSwitches(): Boolean {
    val context = this.context ?: return defaultAutoSwitches
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoSwitches)
        ?: defaultAutoSwitches
}

suspend fun DataCoordinator.setAutoSwitches(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoSwitches] = value
    }
}

// ---

suspend fun DataCoordinator.getAutoButtons(): Boolean {
    val context = this.context ?: return defaultAutoButtons
    return context.dataStore.data.firstOrNull()?.get(PreferencesKeys.autoButtons)
        ?: defaultAutoButtons
}

suspend fun DataCoordinator.setAutoButtons(value: Boolean) {
    val context = this.context ?: return
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.autoButtons] = value
    }
}