package com.avito.runner.scheduler.metrics.model

import com.avito.runner.service.worker.device.DeviceCoordinate
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

internal fun DeviceWorkerState.Companion.createFinishedStubInstance(
    deviceKey: DeviceCoordinate,
    created: Instant = Instant.ofEpochMilli(0),
    finished: Instant = Instant.ofEpochMilli(1),
    builder: DeviceWorkerState.() -> Unit = {}
): DeviceWorkerState.Finished {
    val createdState = DeviceWorkerState.Created(created, ConcurrentHashMap.newKeySet(), deviceKey)
    createdState.apply(builder)
    return createdState.finish(finished)
}

internal fun DeviceWorkerState.addCompletedTestExecution(
    testKey: TestKey,
    intentionReceived: Instant = Instant.ofEpochMilli(0),
    started: Instant = Instant.ofEpochMilli(0),
    completed: Instant = Instant.ofEpochMilli(0)
) {
    testIntentionReceived(testKey, intentionReceived)
    testStarted(testKey, started)
    testCompleted(testKey, completed)
}
