package com.avito.instrumentation.report

import com.avito.report.FakeReportsApi
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

internal class TestReporterTest {

    private val coordinates: ReportCoordinates = ReportCoordinates.createStubInstance()
    private val device: String = "device"
    private val buildId: String = "123552"

    @Test
    fun `every lost tests reported by separate request when batch size is 1`() {
        val reportsApi = FakeReportsApi()
        val reporter = provideTestReporter(
            reportsApi = reportsApi,
            batchSize = 1,
            buildId = buildId
        )

        val lostTestsToReport = listOf(
            AndroidTest.Lost.createStubInstance(name = "lostTest1", deviceName = device),
            AndroidTest.Lost.createStubInstance(name = "lostTest2", deviceName = device),
            AndroidTest.Lost.createStubInstance(name = "lostTest3", deviceName = device),
            AndroidTest.Lost.createStubInstance(name = "lostTest4", deviceName = device)
        )

        reporter.sendLostTests(lostTests = lostTestsToReport)

        Truth.assertThat(reportsApi.addTestsRequests).containsAtLeastElementsIn(
            lostTestsToReport
                .map {
                    FakeReportsApi.AddTestsRequest(
                        reportCoordinates = coordinates,
                        buildId = buildId,
                        tests = listOf(it)
                    )
                }
        )
    }

    private fun provideTestReporter(
        reportsApi: ReportsApi,
        batchSize: Int,
        buildId: String
    ): Report = Report.Impl(
        reportsApi = reportsApi,
        logger = CILogger.allToStdout,
        reportCoordinates = coordinates,
        batchSize = batchSize,
        buildId = buildId
    )
}
