package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.report.Report
import com.avito.report.StubReport
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.config.createStubInstance
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.StubFilterFactory
import com.avito.runner.scheduler.suite.filter.TestsFilter
import com.avito.runner.scheduler.suite.filter.excludedFilter
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

        assertThat(result.testsToRun.map { it.name }).containsExactly(simpleTestInApk.testName)
    }

    @Test
    fun `test suite - skip test - if rerun enabled and test passed in previous run`() {
        val report = StubReport()
        val testSuiteProvider = createTestSuiteProvider(
            report = report,
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

        val result = report.reportedSkippedTests?.map { it.first.name }

        assertThat(result).isEmpty()
    }

    private fun createTestSuiteProvider(
        report: Report = StubReport(),
        targets: List<TargetConfigurationData> = listOf(TargetConfigurationData.createStubInstance()),
        reportSkippedTests: Boolean = false,
        filterFactory: FilterFactory = StubFilterFactory()
    ): TestSuiteProvider {
        return TestSuiteProvider.Impl(
            report = report,
            targets = targets,
            reportSkippedTests = reportSkippedTests,
            filterFactory = filterFactory
        )
    }
}
