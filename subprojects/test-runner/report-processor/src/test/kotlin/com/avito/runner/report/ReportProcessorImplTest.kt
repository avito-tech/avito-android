package com.avito.runner.report

import com.avito.logger.PrintlnLoggerFactory
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.report.serialize.ReportSerializer
import com.avito.retrace.Stub
import com.avito.runner.artifacts.LegacyTestArtifactsProcessor
import com.avito.runner.artifacts.StubTestArtifactsUploader
import com.avito.runner.artifacts.TestArtifactsProcessor
import com.avito.runner.logcat.LogcatProcessor
import com.avito.runner.logcat.StubLogcatAccessor
import com.avito.runner.model.TestResult
import com.avito.runner.model.success
import com.avito.runner.model.timeout
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName
import com.avito.time.DefaultTimeProvider
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ReportProcessorImplTest {

    private val loggerFactory = PrintlnLoggerFactory
    private val artifactsUploader = StubTestArtifactsUploader()
    private val logcatProcessor = LogcatProcessor.Impl(artifactsUploader, Stub)
    private val reportSerializer = ReportSerializer()

    @Test
    fun `process - returns test with no status and contains timeout message - on test run timeout`() {
        val testCase = TestCase(
            name = TestName("com.avito.Test", "test"),
            deviceName = DeviceName("29")
        )

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to TestStaticDataPackage.createStubInstance()
            ),
            logcatProcessor = logcatProcessor
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.timeout(exceptionMessage = "timeout happened"),
            test = testCase,
            executionNumber = 1,
            logcatAccessor = StubLogcatAccessor
        )

        assertThat<AndroidTest.Lost>(testResult) {
            assertThat(incident).isNotNull()
            assertThat(incident?.chain?.get(0)?.message).contains("timeout")
        }
    }

    @Test
    fun `process - returns ok test with logcat stub - logcat upload not needed`(@TempDir tempDir: File) {
        val testStaticData = TestStaticDataPackage.createStubInstance()

        createReportJson(
            reportDir = tempDir,
            testRuntimeData = TestRuntimeDataPackage.createStubInstance()
        )

        val testCase = TestCase(
            name = testStaticData.name,
            deviceName = testStaticData.device
        )

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to testStaticData
            ),
            logcatProcessor = logcatProcessor
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.success(tempDir),
            test = testCase,
            executionNumber = 1,
            logcatAccessor = StubLogcatAccessor
        )

        assertThat<AndroidTest.Completed>(testResult) {
            assertThat(incident).isNull()
            assertThat(logcat).isEqualTo("logcat not uploaded")
        }
    }

    @Test
    fun `process - returns ok test with logcat stub - no logcat`(@TempDir tempDir: File) {
        val testStaticData = TestStaticDataPackage.createStubInstance()

        createReportJson(
            reportDir = tempDir,
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(incident = Incident.createStubInstance())
        )

        val testCase = TestCase(
            name = testStaticData.name,
            deviceName = testStaticData.device
        )

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to testStaticData
            ),
            logcatProcessor = logcatProcessor
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.success(tempDir),
            test = testCase,
            executionNumber = 1,
            logcatAccessor = StubLogcatAccessor
        )

        assertThat<AndroidTest.Completed>(testResult) {
            assertThat(incident).isNotNull()
            assertThat(logcat).isEqualTo(
                """
                    Logcat is not available:
                    stub description
                    Where : StubLogcatAccessor
                    
                    """.trimIndent()
            )
        }
    }

    private fun createReportProcessor(
        testSuite: Map<TestCase, TestStaticData> = emptyMap(),
        logcatProcessor: LogcatProcessor,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ReportProcessorImpl {
        return ReportProcessorImpl(
            loggerFactory = loggerFactory,
            testSuite = testSuite,
            testArtifactsProcessor = createTestArtifactsProcessor(
                logcatProcessor = logcatProcessor,
                dispatcher = dispatcher
            ),
            logcatProcessor = logcatProcessor,
            timeProvider = DefaultTimeProvider(),
            dispatcher = dispatcher
        )
    }

    private fun createTestArtifactsProcessor(
        logcatProcessor: LogcatProcessor,
        dispatcher: CoroutineDispatcher
    ): TestArtifactsProcessor {
        return LegacyTestArtifactsProcessor(
            reportSerializer = ReportSerializer(),
            logcatProcessor = logcatProcessor,
            dispatcher = dispatcher
        )
    }

    private fun createReportJson(
        reportDir: File,
        testRuntimeData: TestRuntimeData
    ) {
        val reportFile = TestArtifactsProviderFactory.createForTempDir(reportDir)
            .provideReportFile()
            .getOrThrow()

        reportSerializer.serialize(testRuntimeData, reportFile)
    }
}
