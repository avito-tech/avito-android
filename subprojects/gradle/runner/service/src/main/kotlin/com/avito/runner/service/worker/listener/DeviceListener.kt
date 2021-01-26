package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

interface DeviceListener {

    suspend fun onDeviceCreated(device: Device, state: State)

    suspend fun onIntentionReceived(device: Device, intention: Intention)

    suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation)

    suspend fun onStatePrepared(device: Device, state: State)

    suspend fun onTestStarted(device: Device, intention: Intention)

    suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun)

    suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable)

    suspend fun onDeviceDied(device: Device, message: String, reason: Throwable)

    suspend fun onFinished(device: Device)
}
