package com.avito.runner.service.worker

import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.model.DeviceInstallation

sealed class DeviceWorkerMessage {

    data class Result(
        val intentionResult: IntentionResult
    ) : DeviceWorkerMessage()

    data class ApplicationInstalled(
        val installation: DeviceInstallation
    ) : DeviceWorkerMessage()

    data class FailedIntentionProcessing(
        val t: Throwable,
        val intention: Intention
    ) : DeviceWorkerMessage()

    data class WorkerDied(
        val t: Throwable,
        val coordinate: DeviceCoordinate
    ) : DeviceWorkerMessage()
}
