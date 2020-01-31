package com.avito.performance

import com.avito.performance.stats.Stats
import com.avito.performance.stats.compare.TestForComparing
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.model.PerformanceTest
import java.util.Collections.singletonList

internal class PerformanceTestComparator(private val stats: Stats) {

    fun compare(
        currentTests: List<PerformanceTest>,
        previousTests: List<PerformanceTest>
    ): List<ComparedTest.Comparison> {
        return getTestsForComparing(currentTests, previousTests)
            .asSequence()
            .map {
                stats.compare(singletonList(it))
                    .get()
                    .map { comparedResult ->
                        ComparedTest.Comparison(
                            comparedResult.testName,
                            currentTests.find { it.testName == comparedResult.testName }!!.id,
                            comparedResult.series
                        )
                    }
            }
            .toList()
            .flatten()
    }

    private fun getTestsForComparing(
        currentTests: List<PerformanceTest>,
        previousTests: List<PerformanceTest>
    ): List<TestForComparing> {
        return currentTests.mapNotNull { currentTest ->
            previousTests.find { it.testName == currentTest.testName }?.let { previousTest ->
                currentTest.series.mapNotNull { metricToSeries ->
                    val currentSeries = metricToSeries.value
                    val previousSeries = previousTest.series[metricToSeries.key]
                    if (previousSeries == null) {
                        null
                    } else {
                        metricToSeries.key to TestForComparing.Series(
                            previousSeries,
                            currentSeries,
                            SIGNIFICANCE
                        )
                    }
                }.toMap()
                    .let { series ->
                        TestForComparing(currentTest.testName, series)
                    }
            }
        }.toList()
    }

}

private const val SIGNIFICANCE = 0.05
