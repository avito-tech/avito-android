package com.avito.performance

import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.ReportsApi
import kotlin.math.abs

internal class PerformanceTestReporter(
    private val reports: ReportsApi
) {
    fun reportSuccess(comparisons: List<ComparedTest.Comparison>) {
        comparisons.forEach { comparison ->
            val isFailed = comparison.failed().isNotEmpty()
            val message = getMessage(comparison.series)
            if (isFailed) {
                markAsFailed(comparison.id, message)
            } else {
                markAsSuccessfull(comparison.id, message)
            }
        }

    }

    private fun getMessage(comparisons: Map<String, ComparedTest.Series>): String {
        return comparisons.toList()
            .joinToString(separator = NEW_LINE) { comparison ->
                "${comparison.first} has changed with significance: ${comparison.second.significance}\n:" +
                    "p-value:\t${comparison.second.pValue}\n" +
                    "threshold:\t${comparison.second.threshold}\n" +
                    "mean diff (current - previous):\t${comparison.second.meanDiff}\n"
            }
    }

    private fun markAsSuccessfull(id: String, message: String) {
        reports.markAsSuccessful(
            testRunId = id,
            author = PERFORMER,
            comment = message
        )
    }

    private fun markAsFailed(id: String, message: String) {
        reports.markAsFailed(
            testRunId = id,
            author = PERFORMER,
            comment = message
        )
    }
}

internal enum class Metric(
    val key: String,
    val shouldBeLess: Boolean,
    val diffThreshold: Double,
    val isBlocker: Boolean
) {
    AVITO_STARTUP_TIME(
        "AvitoStartupTime",
        shouldBeLess = true,
        diffThreshold = 500.0,
        isBlocker = true
    ),
    FPS(
        "FPS",
        shouldBeLess = false,
        diffThreshold = 3.0,
        isBlocker = true
    ),
    PROPER_FRAMES_PERCENT(
        "ProperFramesPercent",
        shouldBeLess = false,
        diffThreshold = 100.0,
        isBlocker = false
    ),
    ACTIVITY_STARTUP_TIME(
        "ActivityStartupTime",
        shouldBeLess = true,
        diffThreshold = 150.0,
        isBlocker = true
    ),
    JUNKY_FRAME_MAX_DURATION(
        "JunkyFrameMaxDuration",
        shouldBeLess = true,
        diffThreshold = 50.0,
        isBlocker = false
    );
}

internal fun getMetric(key: String): Metric? {
    for (metric in Metric.values()) {
        if (metric.key == key) {
            return metric
        }
    }
    return null
}

private fun Map.Entry<String, ComparedTest.Series>.greaterThanExpected(metric: Metric): Boolean {
    return this.value.currentSampleIs == ComparedTest.State.GREATER && metric.shouldBeLess
}

private fun Map.Entry<String, ComparedTest.Series>.lessThanExpected(metric: Metric): Boolean {
    return this.value.currentSampleIs == ComparedTest.State.LESS && metric.shouldBeLess.not()
}

private fun significantThreshold(value: Double, threshold: Double) = abs(threshold) < abs(value)

internal fun ComparedTest.Comparison.failed() =
    this.series
        .filter {
            val metric = getMetric(it.key)
            if (metric == null) {
                false
            } else {
                val greaterThanExpected = it.greaterThanExpected(metric)
                val lessThanExpected = it.lessThanExpected(metric)
                val significantThreshold = significantThreshold(
                    value = it.value.meanDiff,
                    threshold = metric.diffThreshold
                )
                val blocker = metric.isBlocker
                ((lessThanExpected || greaterThanExpected)
                    && significantThreshold
                    && blocker)
            }
        }

internal fun ComparedTest.Comparison.performedMuchBetterThanUsual() =
    this.series
        .filter {
            val metric = getMetric(it.key)
            if (metric == null) {
                false
            } else {
                val greaterThanExpected = it.greaterThanExpected(metric)
                val lessThanExpected = it.lessThanExpected(metric)
                val significantThreshold = significantThreshold(
                    value = it.value.meanDiff,
                    threshold = metric.diffThreshold
                )
                val blocker = metric.isBlocker
                (((lessThanExpected || greaterThanExpected).not())
                    && significantThreshold
                    && blocker)
            }
        }

private const val NEW_LINE = "\n"
private const val PERFORMER = "Performer"
