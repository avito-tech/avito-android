package com.avito.runner.scheduler.metrics.model

internal fun TestTimestamps.Companion.createStubInstance(
    onDevice: Long? = null,
    started: Long? = null,
    finished: Long? = null
) = TestTimestamps(onDevice = onDevice, started = started, finished = finished)
