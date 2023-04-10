package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator.JavaCompileAggregator
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator.KaptGenerateStubAggregator
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator.KaptWithoutKotlincAggregator
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator.KotlinCompileAggregator
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.aggregator.KspAggregator
import java.time.Duration

internal class CompileMetricsTracker(
    private val tracker: BuildMetricSender,
    private val minimumDuration: Duration,
) : BuildOperationsResultListener {

    private val aggregators = listOf(
        JavaCompileAggregator,
        KotlinCompileAggregator,
        KaptGenerateStubAggregator,
        KaptWithoutKotlincAggregator,
        KspAggregator,
    )

    override fun onBuildFinished(result: BuildOperationsResult) {
        val filteredByDuration = result.tasksExecutions
            .filter { it.elapsedMs >= minimumDuration.toMillis() }

        aggregators.forEach { aggregator ->
            filteredByDuration
                .filter(aggregator.filter)
                .map(aggregator.transform)
                .forEach { metric ->
                    tracker.send(metric)
                }
        }
    }
}
