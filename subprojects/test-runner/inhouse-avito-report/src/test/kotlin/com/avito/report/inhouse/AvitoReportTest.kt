package com.avito.report.inhouse

import com.avito.logger.PrintlnLoggerFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.StubReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.model.TestName
import com.avito.time.TimeMachineProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AvitoReportTest {

    private val reportsApi = StubReportsApi()
    private val timeProvider = TimeMachineProvider()
    private val reportCoordinates = ReportCoordinates(
        planSlug = "AvitoAndroid",
        jobSlug = "FunctionalTests",
        runId = "testRunId"
    )
    private val report = AvitoReport(
        reportsApi = reportsApi,
        reportViewerUrl = "",
        loggerFactory = PrintlnLoggerFactory,
        reportCoordinates = reportCoordinates,
        buildId = "testBuildId",
        timeProvider = timeProvider,
        batchSize = 2,
    )

    @Test
    fun `report 3 skipped tests with batchSize 2 - 2 batches sent`() {
        val tests = listOf(
            TestStaticDataPackage.createStubInstance(name = TestName("Test", "test0")),
            TestStaticDataPackage.createStubInstance(name = TestName("Test", "test1")),
            TestStaticDataPackage.createStubInstance(name = TestName("Test", "test2")),
        )
        val skippedTests = tests.mapIndexed { index, test ->
            test to "reason $index"
        }
        report.addSkippedTests(skippedTests)

        assertThat(reportsApi.addTestsRequests.size).isEqualTo(2)
        val batchOfTwo = reportsApi.addTestsRequests.first { it.tests.size == 2 }
        val batchOfOne = reportsApi.addTestsRequests.first { it.tests.size == 1 }
        assertThat(batchOfTwo).isEqualTo(
            StubReportsApi.AddTestsRequest(
                reportCoordinates = reportCoordinates,
                buildId = "testBuildId",
                tests = listOf(
                    AndroidTest.Skipped.fromTestMetadata(
                        testStaticData = tests[0],
                        skipReason = "reason 0",
                        reportTime = 0
                    ),
                    AndroidTest.Skipped.fromTestMetadata(
                        testStaticData = tests[1],
                        skipReason = "reason 1",
                        reportTime = 0
                    ),
                )
            )
        )
        assertThat(batchOfOne).isEqualTo(
            StubReportsApi.AddTestsRequest(
                reportCoordinates = reportCoordinates,
                buildId = "testBuildId",
                tests = listOf(
                    AndroidTest.Skipped.fromTestMetadata(
                        testStaticData = tests[2],
                        skipReason = "reason 2",
                        reportTime = 0
                    ),
                )
            )
        )
        val batchOfOneTest = batchOfOne.tests.first() as AndroidTest.Skipped
        assertThat(batchOfOneTest.skipReason).isEqualTo("reason 2")
    }
}
