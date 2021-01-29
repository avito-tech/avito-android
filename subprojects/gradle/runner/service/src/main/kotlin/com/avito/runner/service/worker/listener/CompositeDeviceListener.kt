package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

internal class CompositeDeviceListener(private val listeners: List<DeviceListener>) : DeviceListener {

    override suspend fun onDeviceCreated(device: Device, state: State) {
        listeners.forEach { it.onDeviceCreated(device, state) }
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        listeners.forEach { it.onIntentionReceived(device, intention) }
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        listeners.forEach { it.onApplicationInstalled(device, installation) }
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        listeners.forEach { it.onStatePrepared(device, state) }
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        listeners.forEach { it.onTestStarted(device, intention) }
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        listeners.forEach { it.onTestCompleted(device, intention, result) }
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        listeners.forEach { it.onIntentionFail(device, intention, reason) }
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        listeners.forEach { it.onDeviceDied(device, message, reason) }
    }

    override suspend fun onFinished(device: Device) {
        listeners.forEach { it.onFinished(device) }
    }
}
