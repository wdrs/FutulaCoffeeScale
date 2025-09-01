// ktlint-disable filename
package com.tomatishe.futulacoffeescale.coordinators.dataCoordinator

suspend fun DataCoordinator.updateAutoAll(value: Boolean) {
    // Update Value
    this.autoAll = value
    // Save to System
    setAutoAll(value)
}

suspend fun DataCoordinator.updateAutoStart(value: Boolean) {
    // Update Value
    this.autoStart = value
    // Save to System
    setAutoStart(value)
}

suspend fun DataCoordinator.updateIgnoreDose(value: Boolean) {
    // Update Value
    this.ignoreDose = value
    // Save to System
    setIgnoreDose(value)
}

suspend fun DataCoordinator.updateAutoDose(value: Boolean) {
    // Update Value
    this.autoDose = value
    // Save to System
    setAutoDose(value)
}

suspend fun DataCoordinator.updateAutoTare(value: Boolean) {
    // Update Value
    this.autoTare = value
    // Save to System
    setAutoTare(value)
}

suspend fun DataCoordinator.updateAutoSwitches(value: Boolean) {
    // Update Value
    this.autoSwitches = value
    // Save to System
    setAutoSwitches(value)
}

suspend fun DataCoordinator.updateAutoButtons(value: Boolean) {
    // Update Value
    this.autoButtons = value
    // Save to System
    setAutoButtons(value)
}

suspend fun DataCoordinator.updateReplaceResetWithTare(value: Boolean) {
    // Update Value
    this.replaceResetWithTare = value
    // Save to System
    setReplaceResetWithTare(value)
}

suspend fun DataCoordinator.updateStopTimerWhenLostConnection(value: Boolean) {
    // Update Value
    this.stopTimerWhenLostConnection = value
    // Save to System
    setStopTimerWhenLostConnection(value)
}

suspend fun DataCoordinator.updateStartSearchAfterLaunch(value: Boolean) {
    // Update Value
    this.startSearchAfterLaunch = value
    // Save to System
    setStartSearchAfterLaunch(value)
}

suspend fun DataCoordinator.updateOneGraphInHistory(value: Boolean) {
    // Update Value
    this.oneGraphInHistory = value
    // Save to System
    setOneGraphInHistory(value)
}

suspend fun DataCoordinator.updateEnableServerWeight(value: Boolean) {
    // Update Value
    this.enableServerWeight = value
    // Save to System
    setEnableServerWeight(value)
}

suspend fun DataCoordinator.updateWeightChartType(value: String) {
    // Update Value
    this.weightChartType = value
    // Save to System
    setWeightChartType(value)
}

suspend fun DataCoordinator.updateFlowRateChartType(value: String) {
    // Update Value
    this.flowRateChartType = value
    // Save to System
    setFlowRateChartType(value)
}

// --- Unit Settings Update Functions ---

suspend fun DataCoordinator.updateUseFixedUnit(value: Boolean) {
    // Update Value
    this.useFixedUnit = value
    // Save to System
    setUseFixedUnit(value)
}

suspend fun DataCoordinator.updateFixedUnitValue(value: String) {
    // Update Value
    this.fixedUnitValue = value
    // Save to System
    setFixedUnitValue(value)
}

suspend fun DataCoordinator.updateAlwaysConvertUnits(value: Boolean) {
    // Update Value
    this.alwaysConvertUnits = value
    // Save to System
    setAlwaysConvertUnits(value)
}

suspend fun DataCoordinator.updatePreferredDisplayUnit(value: String) {
    // Update Value
    this.preferredDisplayUnit = value
    // Save to System
    setPreferredDisplayUnit(value)
}
