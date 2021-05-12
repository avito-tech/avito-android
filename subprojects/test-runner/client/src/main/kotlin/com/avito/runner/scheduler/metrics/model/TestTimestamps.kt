package com.avito.runner.scheduler.metrics.model

internal data class TestTimestamps(
    val onDevice: Long? = null,
    val started: Long? = null,
    val finished: Long? = null
) {
    val effectiveWorkTime: Long? = if (finished != null && onDevice != null) {
        finished - onDevice
    } else {
        null
    }

    val installationTime: Long? = if (started != null && onDevice != null) {
        started - onDevice
    } else {
        null
    }

    val executionTime: Long? = if (finished != null && started != null) {
        finished - started
    } else {
        null
    }

    companion object
}
