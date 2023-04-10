package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator

import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.BaseCompileMetric
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.KaptGenerateStubsMetric
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask

internal object KaptGenerateStubAggregator : BaseCompileMetricAggregator {
    override val filter: (TaskExecutionResult) -> Boolean = { task ->
        task.type == KaptGenerateStubsTask::class.java
    }
    override val transform: (TaskExecutionResult) -> BaseCompileMetric = { task ->
        KaptGenerateStubsMetric(
            task.elapsedMs,
            task.path.module.toTagValue(),
            task.name,
        )
    }
}
