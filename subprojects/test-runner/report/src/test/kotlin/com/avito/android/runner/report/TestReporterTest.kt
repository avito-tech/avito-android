package com.avito.android.runner.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.StubReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestReporterTest {

    private val coordinates: ReportCoordinates = ReportCoordinates.createStubInstance()
    private val device: String = "device"
    private val buildId: String = "123552"

    @Test
    fun `every lost tests reported by separate request when batch size is 1`() {
        val reportsApi = StubReportsApi()

        val reporter = LegacyReport.createStubInstance(
            reportsApi = reportsApi,
            batchSize = 1,
            buildId = buildId,
            reportCoordinates = coordinates
        )

        val lostTestsToReport = listOf(
            AndroidTest.Lost.createStubInstance(methodName = "lostTest1", deviceName = device),
            AndroidTest.Lost.createStubInstance(methodName = "lostTest2", deviceName = device),
            AndroidTest.Lost.createStubInstance(methodName = "lostTest3", deviceName = device),
            AndroidTest.Lost.createStubInstance(methodName = "lostTest4", deviceName = device)
        )

        reporter.reportLostTests(notReportedTests = lostTestsToReport)

        assertThat(reportsApi.addTestsRequests).containsAtLeastElementsIn(
            lostTestsToReport
                .map {
                    StubReportsApi.AddTestsRequest(
                        reportCoordinates = coordinates,
                        buildId = buildId,
                        tests = listOf(it)
                    )
                }
        )
    }
}
