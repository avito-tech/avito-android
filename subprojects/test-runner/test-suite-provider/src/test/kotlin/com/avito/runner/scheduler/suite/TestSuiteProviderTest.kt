package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.StubFilterFactory
import com.avito.test.model.DeviceName
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

        assertThat(result.skippedTests).isEmpty()
    }

    private fun createTestSuiteProvider(
        targets: List<ReportViewerTestStaticDataParser.TargetDevice> = listOf(
            ReportViewerTestStaticDataParser.TargetDevice(
                name = DeviceName("functional-24"),
                api = 22
            )
        ),
        filterFactory: FilterFactory = StubFilterFactory()
    ): TestSuiteProvider {
        return TestSuiteProvider.Impl(
            filterFactory = filterFactory,
            testStaticParser = ReportViewerTestStaticDataParser.Impl(
                targets = targets
            )
        )
    }
}
