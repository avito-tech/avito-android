package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.StatsDConfig
import com.avito.graphite.series.SeriesName
import java.io.Serializable

public data class RunnerMetricsConfig(
    public val statsDConfig: StatsDConfig,
    public val runnerPrefix: SeriesName
) : Serializable
