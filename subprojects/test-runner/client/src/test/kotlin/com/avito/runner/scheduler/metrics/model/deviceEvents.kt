package com.avito.runner.scheduler.metrics.model

import java.time.Instant

internal fun DeviceWorkerEvent.Companion.createStubInstance(
    created: Instant = Instant.ofEpochMilli(0),
    testExecutionEvents: MutableMap<TestKey, TestExecutionEvent> = mutableMapOf(),
    finished: Instant = Instant.ofEpochMilli(0)
) = DeviceWorkerEvent.Finished(created = created, testExecutionEvents = testExecutionEvents, finished = finished)
