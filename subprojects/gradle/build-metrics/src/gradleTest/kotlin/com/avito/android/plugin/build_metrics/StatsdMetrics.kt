package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertWithMessage

private const val loggerPrefix = "[StatsDSender@:] "

internal fun TestResult.expectMetric(type: String, metricName: String) {
    val metrics = statsdMetrics()
    val filtered = filter(metrics, type, metricName)

    assertWithMessage("Expected metric $type $metricName in $metrics")
        .that(filtered).hasSize(1)
}

private fun filter(metrics: List<MetricRecord>, type: String, metricName: String): List<MetricRecord> {
    return metrics
        .filter { it.type == type }
        .filter { it.name.endsWith(".$metricName") }
}

/**
 * Example:
 * ... time:apps.mobile.statistic.android.local.user.id.success.init_configuration.total:5821
 */
// TODO: Intercept on network layer by simulating statsd server
internal fun TestResult.statsdMetrics(): List<MetricRecord> {
    return output.lines().asSequence()
        .filter { it.contains(loggerPrefix) }
        .map { it.substringAfter(loggerPrefix) }
        .map { it.substringAfter("event: ") }
        .map { it.substringBeforeLast(':') }
        .map { line ->
            val type = line.substringBefore(':')
            val name = line.substringAfter(':')
            MetricRecord(type, name)
        }
        .toList()
}

internal data class MetricRecord(val type: String, val name: String)
