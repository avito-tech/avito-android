package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator

import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.BaseCompileMetric
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.KaptWithoutKotlincMetric
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask

internal object KaptWithoutKotlincAggregator : BaseCompileMetricAggregator {
    override val filter: (TaskExecutionResult) -> Boolean = { task ->
        task.type == KaptWithoutKotlincTask::class.java
    }
    override val transform: (TaskExecutionResult) -> BaseCompileMetric = { task ->
        KaptWithoutKotlincMetric(
            task.elapsedMs,
            task.path.module.toTagValue(),
            task.name,
        )
    }
}
