package com.avito.runner.scheduler.metrics.model

internal sealed class TestTimestamps {

    data class NotStarted(val onDevice: Long) : TestTimestamps()

    data class Started(val onDevice: Long, val startTime: Long) : TestTimestamps()

    data class Finished(val onDevice: Long, val startTime: Long, val finishTime: Long) : TestTimestamps() {
        val effectiveWorkTime = finishTime - onDevice
        val installationTime = startTime - onDevice
    }
}

internal fun TestTimestamps.start(currentTimeMillis: Long): TestTimestamps = when (this) {
    is TestTimestamps.NotStarted -> TestTimestamps.Started(this.onDevice, currentTimeMillis)
    is TestTimestamps.Started -> error("Can't start already finished $this")
    is TestTimestamps.Finished -> error("Can't start already started $this")
}

internal fun TestTimestamps.finish(currentTimeMillis: Long): TestTimestamps = when (this) {
    is TestTimestamps.NotStarted -> error("Can't finish not started $this")
    is TestTimestamps.Started -> TestTimestamps.Finished(this.onDevice, this.startTime, currentTimeMillis)
    is TestTimestamps.Finished -> error("Can't finish already finished $this")
}
