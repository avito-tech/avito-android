package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator

import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.BaseCompileMetric

internal interface BaseCompileMetricAggregator {
    val filter: (TaskExecutionResult) -> Boolean
    val transform: (TaskExecutionResult) -> BaseCompileMetric
}
