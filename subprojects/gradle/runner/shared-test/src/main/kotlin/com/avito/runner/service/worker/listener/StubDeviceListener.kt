package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

object StubDeviceListener : DeviceListener {

    override suspend fun onDeviceCreated(device: Device, state: State) {
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
    }

    override suspend fun onFinished(device: Device) {
    }
}
