// ktlint-disable filename
package com.tomatishe.futulacoffeescale.coordinators.dataCoordinator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun DataCoordinator.updateAutoAll(value: Boolean) {
    // Update Value
    this.autoAll = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoAll(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateAutoStart(value: Boolean) {
    // Update Value
    this.autoStart = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoStart(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateIgnoreDose(value: Boolean) {
    // Update Value
    this.ignoreDose = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setIgnoreDose(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateAutoDose(value: Boolean) {
    // Update Value
    this.autoDose = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoDose(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateAutoTare(value: Boolean) {
    // Update Value
    this.autoTare = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoTare(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateAutoSwitches(value: Boolean) {
    // Update Value
    this.autoSwitches = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoSwitches(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateAutoButtons(value: Boolean) {
    // Update Value
    this.autoButtons = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setAutoButtons(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateReplaceResetWithTare(value: Boolean) {
    // Update Value
    this.replaceResetWithTare = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setReplaceResetWithTare(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateStopTimerWhenLostConnection(value: Boolean) {
    // Update Value
    this.stopTimerWhenLostConnection = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setStopTimerWhenLostConnection(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateStartSearchAfterLaunch(value: Boolean) {
    // Update Value
    this.startSearchAfterLaunch = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setStartSearchAfterLaunch(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateOneGraphInHistory(value: Boolean) {
    // Update Value
    this.oneGraphInHistory = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setOneGraphInHistory(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateEnableServerWeight(value: Boolean) {
    // Update Value
    this.enableServerWeight = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setEnableServerWeight(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateWeightChartType(value: String) {
    // Update Value
    this.weightChartType = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setWeightChartType(value)
        // OPTIONAL - Send Broadcast
    }
}

fun DataCoordinator.updateFlowRateChartType(value: String) {
    // Update Value
    this.flowRateChartType = value
    // Save to System
    GlobalScope.launch(Dispatchers.Default) {
        // Update DataStore
        setFlowRateChartType(value)
        // OPTIONAL - Send Broadcast
    }
}
