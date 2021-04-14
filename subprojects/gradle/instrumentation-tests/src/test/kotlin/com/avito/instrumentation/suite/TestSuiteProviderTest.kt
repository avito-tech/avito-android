package com.avito.instrumentation.suite

import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.StubReport
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.suite.TestSuiteProvider
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.TestsFilter
import com.avito.instrumentation.stub.suite.filter.StubFilterFactory
import com.avito.instrumentation.stub.suite.filter.excludedFilter
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestSuiteProviderTest {

    private val simpleTestInApk = TestInApk.createStubInstance(
        className = "com.MyTestClass",
        methodName = "test",
        annotations = emptyList()
    )

    @Test
    fun `test suite - dont skip tests`() {
        val testSuiteProvider = createTestSuiteProvider()

        val result = testSuiteProvider.getTestSuite(
            tests = listOf(simpleTestInApk)
        )

        assertThat(result.testsToRun.map { it.test.name }).containsExactly(simpleTestInApk.testName)
    }

    @Test
    fun `test suite - skip test - if rerun enabled and test passed in previous run`() {
        val report = StubReport()
        val testSuiteProvider = createTestSuiteProvider(
            legacyReport = report,
            reportSkippedTests = true,
            filterFactory = StubFilterFactory(
                filter = excludedFilter(
                    TestsFilter.Result.Excluded.MatchesExcludeSignature(
                        name = "",
                        source = TestsFilter.Signatures.Source.PreviousRun
                    )
                )
            )
        )

        testSuiteProvider.getTestSuite(
            tests = listOf(simpleTestInApk)
        )

        assertThat(report.reportedSkippedTests?.map { it.first.name })
            .isEmpty()
    }

    private fun createTestSuiteProvider(
        report: Report = StubReport(),
        legacyReport: LegacyReport = StubReport(),
        targets: List<TargetConfiguration.Data> = listOf(TargetConfiguration.Data.createStubInstance()),
        reportSkippedTests: Boolean = false,
        filterFactory: FilterFactory = StubFilterFactory(),
        timeProvider: TimeProvider = StubTimeProvider(),
        useInMemoryReport: Boolean = false
    ): TestSuiteProvider {
        return TestSuiteProvider.Impl(
            report = report,
            legacyReport = legacyReport,
            targets = targets,
            reportSkippedTests = reportSkippedTests,
            filterFactory = filterFactory,
            timeProvider = timeProvider,
            useInMemoryReport = useInMemoryReport
        )
    }
}
