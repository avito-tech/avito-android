package com.avito.performance

import com.avito.report.ReportsApi
import com.avito.report.model.PerformanceTest
import com.avito.report.model.ReportCoordinates
import kotlin.streams.toList

internal class PerformanceTestCollector(
    private val reports: ReportsApi,
    private val coordinates: ReportCoordinates,
    private val buildId: String?
) {

    fun collect(): List<PerformanceTest> {

        val report = reports.getTestsForRunId(coordinates)

        return report.get()
            .parallelStream()
            .filter {
                if (buildId != null) {
                    it.buildId == buildId
                } else {
                    true
                }
            }
            .map { reports.getPerformanceTest(it.id).get() }
            .toList()
    }
}
