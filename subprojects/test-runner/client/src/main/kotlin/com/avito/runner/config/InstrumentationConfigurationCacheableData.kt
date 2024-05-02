package com.avito.runner.config

import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import java.io.Serializable
import java.time.Duration

public data class InstrumentationConfigurationCacheableData(
    val name: String,
    val reportSkippedTests: Boolean,
    val targets: List<TargetConfigurationCacheableData>,
    val testRunnerExecutionTimeout: Duration,
    val instrumentationTaskTimeout: Duration,
    val singleTestRunTimeout: Duration,
    val filter: InstrumentationFilterData,
) : Serializable
