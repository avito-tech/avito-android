package com.avito.instrumentation.report

import com.avito.instrumentation.suite.filter.TestRunFilter
import com.avito.report.FakeReportsApi
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Status
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestReporterTest {

    private val coordinates: ReportCoordinates = ReportCoordinates.createStubInstance()
    private val device: String = "device"
    private val buildId: String = "123552"

    @Test
    fun `skipped tests with reason passed in previous run have not reported`() {
        val reportsApi = FakeReportsApi()
        val reporter = provideTestReporter(
            reportsApi = reportsApi,
            buildId = buildId
        )

        val skippedTestsToReport = listOf(
            TestStaticDataPackage.createStubInstance(
                name = "skippedBecauseOfIgnored",
                deviceName = device
            ) to TestRunFilter.Verdict.Skip.Ignored,
            TestStaticDataPackage.createStubInstance(
                name = "skippedBecauseOfAlreadyPassed",
                deviceName = device
            ) to TestRunFilter.Verdict.Skip.OnlyFailed(status = Status.Success),
            TestStaticDataPackage.createStubInstance(
                name = "skippedBecauseOfNotSpecifiedInFile",
                deviceName = device
            ) to TestRunFilter.Verdict.Skip.NotSpecifiedInFile
        )

        reporter.sendSkippedTests(skippedTests = skippedTestsToReport)

        assertThat(reportsApi.addTestsRequests).containsExactly(
            FakeReportsApi.AddTestsRequest(
                reportCoordinates = coordinates,
                buildId = buildId,
                tests = skippedTestsToReport
                    .filter {
                        it.second !is TestRunFilter.Verdict.Skip.OnlyFailed
                    }
                    .map {
                        AndroidTest.Skipped.fromTestMetadata(it.first, it.second.description, 0)
                    }
            )
        )
    }

    @Test
    fun `every skipped tests reported by separate request when batch size is 1`() {
        val reportsApi = FakeReportsApi()
        val reporter = provideTestReporter(
            reportsApi = reportsApi,
            batchSize = 1,
            buildId = buildId
        )

        val skippedTestsToReport = listOf(
            TestStaticDataPackage.createStubInstance(
                "skippedBecauseOfIgnored",
                device
            ) to TestRunFilter.Verdict.Skip.Ignored,
            TestStaticDataPackage.createStubInstance(
                "skippedBecauseOfIgnored",
                device
            ) to TestRunFilter.Verdict.Skip.Ignored,
            TestStaticDataPackage.createStubInstance(
                "skippedBecauseOfIgnored",
                device
            ) to TestRunFilter.Verdict.Skip.Ignored,
            TestStaticDataPackage.createStubInstance(
                "skippedBecauseOfIgnored",
                device
            ) to TestRunFilter.Verdict.Skip.Ignored
        )

        reporter.sendSkippedTests(skippedTests = skippedTestsToReport)

        assertThat(reportsApi.addTestsRequests).containsAtLeastElementsIn(
            skippedTestsToReport
                .map {
                    FakeReportsApi.AddTestsRequest(
                        reportCoordinates = coordinates,
                        buildId = buildId,
                        tests = listOf(
                            AndroidTest.Skipped.fromTestMetadata(it.first, it.second.description, 0)
                        )
                    )
                }
        )
    }

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

        assertThat(reportsApi.addTestsRequests).containsAtLeastElementsIn(
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
        batchSize: Int = 200,
        buildId: String
    ): Report = Report.Impl(
        reportsApi = reportsApi,
        logger = CILogger.allToStdout,
        reportCoordinates = coordinates,
        batchSize = batchSize,
        buildId = buildId
    )
}
