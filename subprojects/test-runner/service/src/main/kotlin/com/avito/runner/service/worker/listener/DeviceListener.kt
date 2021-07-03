package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

public interface DeviceListener {

    public suspend fun onDeviceCreated(device: Device, state: State)

    public suspend fun onIntentionReceived(device: Device, intention: Intention)

    public suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation)

    public suspend fun onStatePrepared(device: Device, state: State)

    public suspend fun onTestStarted(device: Device, intention: Intention)

    public suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun)

    public suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable)

    public suspend fun onDeviceDied(device: Device, message: String, reason: Throwable)

    public suspend fun onFinished(device: Device)
}
