package com.avito.runner.service.worker.listener

import com.avito.logger.Logger
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

internal class DeviceLogListener(private val deviceLogger: Logger) : DeviceListener {

    override suspend fun onDeviceCreated(device: Device, state: State) {
        deviceLogger.debug("Device is online ($state)")
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        deviceLogger.debug("Received intention: $intention")
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        deviceLogger.debug("Application installed: ${installation.installation}")
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        deviceLogger.debug("State prepared: ($state)")
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        deviceLogger.debug("Worker test run started for intention: $intention")
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        deviceLogger.debug("Worker test run completed for intention: $intention")
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        deviceLogger.warn("Device can't process intention: $intention", reason)
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        deviceLogger.warn("Device died: $message", reason)
    }

    override suspend fun onFinished(device: Device) {
        deviceLogger.debug("Worker ended with success result")
    }
}
