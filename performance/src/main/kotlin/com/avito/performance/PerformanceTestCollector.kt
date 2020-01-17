package com.avito.performance

import com.avito.report.ReportsApi
import com.avito.report.model.PerformanceTest
import com.avito.report.model.ReportCoordinates
import kotlin.streams.toList

internal class PerformanceTestCollector(
    private val reports: ReportsApi,
    private val id: ReportCoordinates,
    private val buildId: String
) {

    fun collect(): List<PerformanceTest> {

        val report = reports.getTestsForRunId(id)

        return report.get()
            .parallelStream()
            .filter { it.buildId == buildId }
            .map { reports.getPerformanceTest(it.id).get() }
            .toList()
    }
}
