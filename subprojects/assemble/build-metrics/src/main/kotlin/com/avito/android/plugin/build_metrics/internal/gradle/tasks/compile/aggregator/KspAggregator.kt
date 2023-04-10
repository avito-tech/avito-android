package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator

import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.BaseCompileMetric
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.KspJvmMetric
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.google.devtools.ksp.gradle.KspTaskJvm

internal object KspAggregator : BaseCompileMetricAggregator {
    override val filter: (TaskExecutionResult) -> Boolean = { task ->
        task.type == KspTaskJvm::class.java
    }
    override val transform: (TaskExecutionResult) -> BaseCompileMetric = { task ->
        KspJvmMetric(
            task.elapsedMs,
            task.path.module.toTagValue(),
            task.name,
        )
    }
}
