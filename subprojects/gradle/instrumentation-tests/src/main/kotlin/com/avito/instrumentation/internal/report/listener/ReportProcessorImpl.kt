package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class ReportProcessorImpl(
    loggerFactory: LoggerFactory,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val metricsSender: InstrumentationMetricsSender,
    private val testArtifactsProcessor: TestArtifactsProcessor,
    private val logcatProcessor: LogcatProcessor,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher
) : ReportProcessor {

    private val logger = loggerFactory.create<ReportProcessorImpl>()

    override fun createTestReport(
        result: TestResult,
        test: TestCase,
        executionNumber: Int,
        logcatBuffer: LogcatBuffer?
    ): AndroidTest {

        val testFromSuite = requireNotNull(testSuite[test]) { "Can't find test in suite: ${test.testName}" }

        return when (result) {
            is TestResult.Complete ->
                result.artifacts
                    .flatMap { reportDir ->
                        testArtifactsProcessor.process(
                            reportDir = reportDir,
                            testStaticData = testFromSuite,
                            logcatBuffer = logcatBuffer
                        )
                    }
                    .rescue { throwable ->
                        val errorMessage = "Can't get report from file: $test"

                        logger.warn(errorMessage, throwable)

                        metricsSender.sendReportFileNotAvailable()

                        processFailure(
                            throwable = throwable,
                            testStaticData = testFromSuite,
                            logcatBuffer = logcatBuffer
                        )
                    }.getOrThrow()

            is TestResult.Incomplete ->
                with(result.infraError) {
                    logger.warn("${error.message} while executing ${test.testName}", this.error)

                    when (this) {
                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnParsing ->
                            metricsSender.sendFailedOnParsingInstrumentation()

                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnStart ->
                            metricsSender.sendFailedOnStartInstrumentation()

                        is TestCaseRun.Result.Failed.InfrastructureError.Timeout ->
                            metricsSender.sendTimeOut()

                        is TestCaseRun.Result.Failed.InfrastructureError.Unexpected ->
                            metricsSender.sendUnexpectedInfraError()
                    }

                    processFailure(
                        throwable = error,
                        testStaticData = testFromSuite,
                        logcatBuffer = logcatBuffer
                    ).getOrThrow()
                }
        }
    }

    private fun processFailure(
        throwable: Throwable,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest> {
        val scope = CoroutineScope(CoroutineName("test-artifacts-failure-${testStaticData.name}") + dispatcher)

        return runBlocking {
            withContext(scope.coroutineContext) {

                val stdout = async {
                    logcatProcessor.process(logcatBuffer?.getStdout(), isUploadNeeded = true)
                }

                val stderr = async {
                    logcatProcessor.process(logcatBuffer?.getStderr(), isUploadNeeded = true)
                }

                Result.Success(
                    AndroidTest.Lost.fromTestStaticData(
                        testStaticData,
                        startTime = 0,
                        lastSignalTime = 0,
                        stdout = stdout.await(),
                        stderr = stderr.await(),
                        incident = Incident(
                            type = Incident.Type.INFRASTRUCTURE_ERROR,
                            timestamp = timeProvider.nowInSeconds(),
                            trace = throwable.stackTraceToList(),
                            chain = listOf(
                                IncidentElement(
                                    message = throwable.message ?: "no error message"
                                )
                            ),
                            entryList = emptyList()
                        )
                    )
                )
            }
        }
    }
}
