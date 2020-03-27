package com.avito.instrumentation.suite

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.report.FakeReport
import com.avito.instrumentation.suite.dex.FakeTestSuiteLoader
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.createStubInstance
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class TestSuiteProviderTest {

    private val testSuiteLoader = FakeTestSuiteLoader()
    private val reporter = FakeReport()
    private val testSuiteProvider: TestSuiteProvider = TestSuiteProvider.Impl(
        report = reporter,
        testSuiteLoader = testSuiteLoader
    )

    private val simpleTestInApk = TestInApk.createStubInstance(
        className = "com.MyTestClass",
        methodName = "test",
        annotations = emptyList()
    )

    @BeforeEach
    private fun setup() {
        testSuiteLoader.result.add(simpleTestInApk)
    }

    @Test
    fun `initial suite - dont skip tests - if rerun enabled, but results unavailable`() {

        val result = testSuiteProvider.getInitialTestSuite(
            testApk = File("."),
            params = InstrumentationTestsAction.Params.createStubInstance(
                instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                    rerunFailedTests = true,
                    targets = listOf(TargetConfiguration.Data.createStubInstance())
                )
            ),
            previousRun = { Try.Failure(Exception("anything")) },
            getTestsByReportId = { Try.Success(emptyList()) }
        )

        assertThat(result.map { it.test.name }).containsExactly(simpleTestInApk.testName)
    }

    @Test
    fun `initial suite - dont skip tests - if rerun enabled, but report is empty`() {

        val result = testSuiteProvider.getInitialTestSuite(
            testApk = File("."),
            params = InstrumentationTestsAction.Params.createStubInstance(
                instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                    rerunFailedTests = true,
                    targets = listOf(TargetConfiguration.Data.createStubInstance())
                )
            ),
            previousRun = { Try.Success(emptyList()) },
            getTestsByReportId = { Try.Success(emptyList()) }
        )

        assertThat(result.map { it.test.name }).containsExactly(simpleTestInApk.testName)
    }

    @Test
    fun `initial suite - dont skip tests - if rerun enabled, but previous report does not contain specific test`() {

        val result = testSuiteProvider.getInitialTestSuite(
            testApk = File("."),
            params = InstrumentationTestsAction.Params.createStubInstance(
                instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                    rerunFailedTests = true,
                    targets = listOf(TargetConfiguration.Data.createStubInstance("api22"))
                )
            ),
            previousRun = {
                Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "some.other.test",
                            deviceName = "someOtherDevice",
                            status = Status.Failure("somethingWentWrong", "111")
                        )
                    )
                )
            },
            getTestsByReportId = { _ -> Try { emptyList<SimpleRunTest>() }}
        )

        assertThat(result.map { it.test.name }).containsExactly(simpleTestInApk.testName)
    }

    @Test
    fun `initial suite - skip test - if rerun enabled and test passed in previous run`() {

        testSuiteProvider.getInitialTestSuite(
            testApk = File("."),
            params = InstrumentationTestsAction.Params.createStubInstance(
                instrumentationConfiguration = InstrumentationConfiguration.Data.createStubInstance(
                    rerunFailedTests = true,
                    targets = listOf(TargetConfiguration.Data.createStubInstance("api25"))
                )
            ),
            previousRun = {
                Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = simpleTestInApk.testName.name,
                            deviceName = "api25",
                            status = Status.Success
                        )
                    )
                )
            },
            getTestsByReportId = { _ -> Try { emptyList<SimpleRunTest>() }}
        )

        assertThat(reporter.reportedSkippedTests?.map { it.first.name })
            .containsExactly(simpleTestInApk.testName)
    }
}
