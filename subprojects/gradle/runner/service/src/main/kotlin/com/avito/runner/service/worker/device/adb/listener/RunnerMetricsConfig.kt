package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDConfig
import java.io.Serializable

data class RunnerMetricsConfig(
    val statsDConfig: StatsDConfig,
    val runnerPrefix: SeriesName
) : Serializable
