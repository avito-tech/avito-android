package com.avito.android.plugin.build_metrics.internal.result

import java.time.Duration
import java.time.Instant

internal class BuildResult(
    val status: BuildStatus,
    val startTime: Instant,
    configurationEndTime: Instant,
    finishTime: Instant,
) {
    val configurationDuration: Duration = Duration.between(startTime, configurationEndTime)
    val buildDuration: Duration = Duration.between(startTime, finishTime)
}
