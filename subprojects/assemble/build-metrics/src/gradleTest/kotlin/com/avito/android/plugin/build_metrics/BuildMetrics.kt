package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestResult
import com.google.common.truth.IterableSubject
import com.google.common.truth.Truth.assertWithMessage

private const val loggerPrefix = "graphite-test "

internal fun TestResult.assertHasMetric(
    path: String,
    value: String? = null,
    time: String? = null,
    quantity: IterableSubject.() -> Unit = { hasSize(1) }
) {
    val metrics = graphiteMetrics()
    val filtered = metrics
        .filter {
            val metricParts = it.split(" ")
            val equalPath = metricParts[0] == path
            val equalValue = value == null || metricParts[1] == value
            val equalTime = time == null || metricParts[2] == time
            equalPath && equalValue && equalTime
        }

    assertWithMessage("Expected metric ($path) in $metrics. Logs: $output")
        .that(filtered).quantity()
}

internal fun TestResult.assertNoMetric(path: String) {
    val metrics = graphiteMetrics()
    val filtered = metrics
        .filter {
            it.contains(path)
        }

    assertWithMessage("Expected no metric ($path) in $metrics")
        .that(filtered).isEmpty()
}

/**
 * Example:
 * graphite-test build.metrics.test.builds.jvm.memory.heap.used;build_type=test;env=ci;process_name=kotlin_daemon 590231 -1
 */
// TODO: Intercept on network layer
internal fun TestResult.graphiteMetrics(): List<String> {
    return output.lines().asSequence()
        .filter { it.contains(loggerPrefix) }
        .map { it.substringAfter(loggerPrefix) }
        .toList()
}
