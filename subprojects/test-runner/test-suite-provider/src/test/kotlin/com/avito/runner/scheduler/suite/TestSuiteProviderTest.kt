package com.avito.runner.scheduler.suite

import com.avito.android.AnnotationData
import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.createStub
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisMode
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisSelection
import com.avito.runner.scheduler.suite.filter.StubFilterFactory
import com.avito.runner.scheduler.suite.filter.StubFilterFactoryFactory
import com.avito.runner.scheduler.suite.filter.createStubInstance
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

    @Test
    fun `targets split by impact analysis - every test runs once and is not skipped on other target`() {
        val changedTarget = DeviceName("api30PRChanged")
        val mainTarget = DeviceName("api30PR")
        val testSuiteProvider = createTestSuiteProvider(
            targets = listOf(
                ReportViewerTestStaticDataParser.TargetDevice(name = mainTarget, api = 30),
                ReportViewerTestStaticDataParser.TargetDevice(name = changedTarget, api = 30),
            ),
            filterFactory = StubFilterFactoryFactory.create(
                filter = InstrumentationFilterData.createStub(),
                impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                    selection = ImpactAnalysisSelection.ByTarget(
                        mapOf(
                            changedTarget.name to ImpactAnalysisMode.CHANGED,
                            mainTarget.name to ImpactAnalysisMode.ALL_EXCEPT_CHANGED,
                        )
                    ),
                    changedTests = listOf("com.Changed"),
                )
            )
        )

        val result = testSuiteProvider.getTestSuite(
            tests = listOf(
                TestInApk.createStubInstance(className = "com.Changed", methodName = "test"),
                TestInApk.createStubInstance(className = "com.NotChanged", methodName = "test"),
                TestInApk.createStubInstance(
                    className = "com.Ignored",
                    methodName = "test",
                    annotations = listOf(AnnotationData("org.junit.Ignore", emptyMap()))
                ),
            )
        )

        assertThat(result.testsToRun.map { it.name.name to it.device }).containsExactly(
            "com.Changed.test" to changedTarget,
            "com.NotChanged.test" to mainTarget,
        )
        assertThat(result.skippedTests.map { (test, _) -> test.name.name to test.device }).containsExactly(
            "com.Ignored.test" to mainTarget,
        )
    }

    @Test
    fun `no changed tests - changed target produces neither executions nor skipped tests`() {
        val mainTarget = DeviceName("api30PR")
        val changedTarget = DeviceName("api30PRChanged")
        val testSuiteProvider = createTestSuiteProvider(
            targets = listOf(
                ReportViewerTestStaticDataParser.TargetDevice(name = mainTarget, api = 30),
                ReportViewerTestStaticDataParser.TargetDevice(name = changedTarget, api = 30),
            ),
            filterFactory = StubFilterFactoryFactory.create(
                filter = InstrumentationFilterData.createStub(),
                impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                    selection = ImpactAnalysisSelection.ByTarget(
                        mapOf(
                            mainTarget.name to ImpactAnalysisMode.ALL_EXCEPT_CHANGED,
                            changedTarget.name to ImpactAnalysisMode.CHANGED,
                        )
                    ),
                    changedTests = emptyList(),
                )
            )
        )

        val result = testSuiteProvider.getTestSuite(
            listOf(
                simpleTestInApk,
                TestInApk.createStubInstance(
                    className = "com.Ignored",
                    methodName = "test",
                    annotations = listOf(AnnotationData("org.junit.Ignore", emptyMap()))
                ),
            )
        )

        assertThat(result.testsToRun.map { it.name.name to it.device }).containsExactly(
            "com.MyTestClass.test" to mainTarget,
        )
        assertThat(result.skippedTests.map { (test, _) -> test.name.name to test.device }).containsExactly(
            "com.Ignored.test" to mainTarget,
        )
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
