package com.avito.runner.scheduler.metrics.model

import java.time.Duration

internal fun DeviceWorkerEvents.Companion.createStubInstance(
    created: Duration = Duration.ofMillis(0),
    testExecutionEvents: MutableMap<TestKey, TestExecutionEvent> = mutableMapOf(),
    finished: Duration = Duration.ofMillis(0)
) = DeviceWorkerEvents.Finished(created = created, testExecutionEvents = testExecutionEvents, finished = finished)
