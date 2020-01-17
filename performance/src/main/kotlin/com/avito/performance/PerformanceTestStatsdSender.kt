package com.avito.performance

import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.model.PerformanceTest
import kotlin.math.abs

internal class PerformanceTestStatsdSender(private val sender: PerformanceMetricSender) {

    fun sendSample(runTests: List<PerformanceTest>) {
        runTests.forEach { test ->
            test.series.forEach { series ->
                val metricName = "${test.testName.graphite()}_${series.key}"
                series.value.forEach { metricValue ->
                    sender.trackSample(metricName, metricValue.toLong())
                }
            }
        }
    }

    fun sendCompare(comparisonList: List<ComparedTest.Comparison>) {
        comparisonList.forEach { comparison ->
            comparison.series.forEach {
                val scaledpValue = it.value.pValue * 10000
                sender.trackComparison("${comparison.testName.graphite()}_${it.key}", (scaledpValue).toLong())
                sender.trackComparison(
                    "${comparison.testName.graphite()}_${it.key}_meanDiff",
                    abs(it.value.meanDiff).toLong()
                )
            }
            sender.trackSuccess(comparison.testName.graphite(), comparison)
        }
    }

    fun reportToPr(comparisonList: List<ComparedTest.Comparison>) {
        comparisonList.forEach { comparison ->
            if (comparison.failed().isNotEmpty()) {
                sender.reportToPr()
                return
            }
        }
    }
}

private fun String.graphite() = this.split("::")[1]
