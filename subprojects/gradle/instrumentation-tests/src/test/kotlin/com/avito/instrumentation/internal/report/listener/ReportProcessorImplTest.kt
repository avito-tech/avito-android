package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StubStatsdSender
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.StubLoggerFactory
import com.avito.report.ReportFileProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.retrace.ProguardRetracer
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.scheduler.listener.success
import com.avito.runner.scheduler.listener.timeout
import com.avito.runner.service.model.TestCase
import com.avito.time.StubTimeProvider
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ReportProcessorImplTest {

    private val loggerFactory = StubLoggerFactory
    private val statsdSender = StubStatsdSender()
    private val artifactsUploader = StubTestArtifactsUploader()
    private val timeProvider = StubTimeProvider()
    private val gson = TestArtifactsProcessor.gson

    @Test
    fun `process - returns test with no status and contains timeout message - on test run timeout`() {
        val testCase = TestCase(className = "com.avito.Test", methodName = "test", deviceName = "29")

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to TestStaticDataPackage.createStubInstance()
            ),
            artifactsUploader = artifactsUploader
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.timeout("timeout happened"),
            test = testCase,
            executionNumber = 1,
            logcatBuffer = null
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
            gson = gson,
            testStaticData = testStaticData,
            testRuntimeData = TestRuntimeDataPackage.createStubInstance()
        )

        val testCase = TestCase(
            className = testStaticData.name.className,
            methodName = testStaticData.name.methodName,
            deviceName = testStaticData.device.name
        )

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to testStaticData
            ),
            artifactsUploader = artifactsUploader
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.success(tempDir),
            test = testCase,
            executionNumber = 1,
            logcatBuffer = null
        )

        assertThat<AndroidTest.Completed>(testResult) {
            assertThat(incident).isNull()
            assertThat(stdout).isEqualTo("logcat not uploaded")
            assertThat(stderr).isEqualTo("logcat not uploaded")
        }
    }

    @Test
    fun `process - returns ok test with logcat stub - logcat upload failed`(@TempDir tempDir: File) {
        val testStaticData = TestStaticDataPackage.createStubInstance()

        createReportJson(
            reportDir = tempDir,
            gson = gson,
            testStaticData = testStaticData,
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(incident = Incident.createStubInstance())
        )

        val testCase = TestCase(
            className = testStaticData.name.className,
            methodName = testStaticData.name.methodName,
            deviceName = testStaticData.device.name
        )

        val postProcessor = createReportProcessor(
            testSuite = mapOf(
                testCase to testStaticData
            ),
            artifactsUploader = object : TestArtifactsUploader {

                override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
                    delay(100)
                    return Result.Success("http://stub".toHttpUrl())
                }

                override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
                    return Result.Success("http://stub".toHttpUrl())
                }
            }
        )

        val testResult = postProcessor.createTestReport(
            result = TestResult.success(tempDir),
            test = testCase,
            executionNumber = 1,
            logcatBuffer = null
        )

        assertThat<AndroidTest.Completed>(testResult) {
            assertThat(incident).isNotNull()
            assertThat(stdout).isEqualTo("logcat not available")
            assertThat(stderr).isEqualTo("logcat not available")
        }
    }

    private fun createReportProcessor(
        testSuite: Map<TestCase, TestStaticData> = emptyMap(),
        artifactsUploader: TestArtifactsUploader
    ): ReportProcessorImpl {
        return ReportProcessorImpl(
            loggerFactory = loggerFactory,
            testSuite = testSuite,
            metricsSender = InstrumentationMetricsSender(statsdSender, SeriesName.create("")),
            testArtifactsProcessor = createTestArtifactsProcessor(artifactsUploader)
        )
    }

    private fun createTestArtifactsProcessor(artifactsUploader: TestArtifactsUploader): TestArtifactsProcessor {
        return LegacyTestArtifactsProcessor(
            gson = gson,
            testArtifactsUploader = artifactsUploader,
            retracer = ProguardRetracer.Stub,
            timeProvider = timeProvider
        )
    }

    private fun createReportJson(
        reportDir: File,
        gson: Gson,
        testStaticData: TestStaticData,
        testRuntimeData: TestRuntimeData
    ) {
        val reportFile = ReportFileProviderFactory.create(lazy { reportDir }, testStaticData)
            .provideReportFile()
            .getOrThrow()

        reportFile.writeText(gson.toJson(testRuntimeData))
    }
}
