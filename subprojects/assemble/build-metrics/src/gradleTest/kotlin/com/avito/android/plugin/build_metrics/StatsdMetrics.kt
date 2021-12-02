package com.avito.android.plugin.build_metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeDoubleMetric
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.TimeMetric
import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertWithMessage

private const val loggerPrefix = "[StatsDSender] "

internal inline fun <reified T : StatsMetric> TestResult.assertHasMetric(path: String): T {
    val type = T::class.java
    val metrics = statsdMetrics()
    val filtered = metrics
        .filterIsInstance(type)
        .filter {
            it.name.toString().contains(path)
        }

    assertWithMessage("Expected metric ${type.simpleName}($path) in $metrics. Logs: $output")
        .that(filtered).hasSize(1)

    return filtered.first()
}

internal inline fun <reified T : StatsMetric> TestResult.assertNoMetric(path: String) {
    val type = T::class.java
    val metrics = statsdMetrics()
    val filtered = metrics
        .filterIsInstance(type)
        .filter {
            it.name.toString().contains(path)
        }

    assertWithMessage("Expected no metric ${type.simpleName}($path) in $metrics")
        .that(filtered).isEmpty()
}

/**
 * Example:
 * ... time:apps.mobile.statistic.android.local.user.id.success.init_configuration.total:5821
 */
// TODO: Intercept on network layer by simulating statsd server
internal fun TestResult.statsdMetrics(): List<StatsMetric> {
    return output.lines().asSequence()
        .filter { it.contains(loggerPrefix) }
        .map { it.substringAfter(loggerPrefix) }
        .map { it.substringAfter("event: ") }
        .map { parseStatsdMetric(it) }
        .toList()
}

private fun parseStatsdMetric(raw: String): StatsMetric {
    val type = raw.substringBefore(':')
    val name = raw.substringBeforeLast(':').substringAfter(':')
    val value = raw.substringAfterLast(':')
    return when (type) {
        "count" -> CountMetric(SeriesName.create(name, multipart = true), value.toLong())
        "time" -> TimeMetric(SeriesName.create(name, multipart = true), value.toLong())
        "gauge" -> if (value.contains('.')) {
            GaugeLongMetric(SeriesName.create(name, multipart = true), value.toLong())
        } else {
            GaugeDoubleMetric(SeriesName.create(name, multipart = true), value.toDouble())
        }
        else -> throw IllegalStateException("Unknown statsd metric: $raw")
    }
}
