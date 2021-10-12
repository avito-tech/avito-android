package com.avito.runner.service.worker.listener

import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunActionResult
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation
import kotlinx.coroutines.channels.SendChannel

internal class MessagesDeviceListener(private val messagesChannel: SendChannel<DeviceWorkerMessage>) : DeviceListener {

    override suspend fun onDeviceCreated(device: Device, state: State) {
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        messagesChannel.send(DeviceWorkerMessage.ApplicationInstalled(installation))
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        messagesChannel.send(
            DeviceWorkerMessage.Result(
                intentionResult = IntentionResult(
                    intention = intention,
                    actionResult = InstrumentationTestRunActionResult(
                        testCaseRun = result
                    )
                )
            )
        )
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        messagesChannel.send(
            DeviceWorkerMessage.FailedIntentionProcessing(
                t = reason,
                intention = intention
            )
        )
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        messagesChannel.send(
            DeviceWorkerMessage.WorkerDied(
                t = reason,
                coordinate = device.coordinate
            )
        )
    }

    override suspend fun onFinished(device: Device) {
        // empty
    }
}
