package com.avito.runner.scheduler.metrics.model

internal fun DeviceTimestamps.Companion.createStubInstance(
    created: Long = 0,
    testTimestamps: MutableMap<TestKey, TestTimestamps> = mutableMapOf(),
    finished: Long = 0
) = DeviceTimestamps(created = created, testTimestamps = testTimestamps, finished = finished)
