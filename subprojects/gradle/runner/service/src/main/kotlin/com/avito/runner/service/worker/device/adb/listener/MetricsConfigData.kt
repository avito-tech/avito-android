package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.StatsDConfig

data class MetricsConfigData(
    val statsDConfig: StatsDConfig,
    val buildId: String,
    val instrumentationConfigName: String
)
