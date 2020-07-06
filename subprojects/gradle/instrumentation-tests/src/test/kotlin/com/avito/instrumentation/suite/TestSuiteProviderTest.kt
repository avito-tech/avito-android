package com.avito.instrumentation.suite

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.report.FakeReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.createStubInstance
import com.avito.instrumentation.suite.filter.FakeFilterFactory
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.instrumentation.suite.filter.TestsFilter
import com.avito.instrumentation.suite.filter.excludedFilter
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
        val report = FakeReport()
        val testSuiteProvider = createTestSuiteProvider(
            report = report,
            reportSkippedTests = true,
            filterFactory = FakeFilterFactory(
                filter = excludedFilter(
                    TestsFilter.Result.Excluded.MatchExcludeSignature(
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
        report: Report = FakeReport(),
        targets: List<TargetConfiguration.Data> = listOf(TargetConfiguration.Data.createStubInstance()),
        reportSkippedTests: Boolean = false,
        filterFactory: FilterFactory = FakeFilterFactory()
    ): TestSuiteProvider {
        return TestSuiteProvider.Impl(
            report = report,
            targets = targets,
            reportSkippedTests = reportSkippedTests,
            filterFactory = filterFactory
        )
    }
}
