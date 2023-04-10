package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator

import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.BaseCompileMetric
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.KotlinCompileMetric
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal object KotlinCompileAggregator : BaseCompileMetricAggregator {
    override val filter: (TaskExecutionResult) -> Boolean = { task ->
        task.type == KotlinCompile::class.java
    }
    override val transform: (TaskExecutionResult) -> BaseCompileMetric = { task ->
        KotlinCompileMetric(
            task.elapsedMs,
            task.path.module.toTagValue(),
            task.name,
        )
    }
}
