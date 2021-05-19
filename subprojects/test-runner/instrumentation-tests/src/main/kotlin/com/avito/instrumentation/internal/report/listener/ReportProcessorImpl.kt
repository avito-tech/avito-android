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
                        val errorMessage = "Can't get report artifacts"

                        logger.warn(errorMessage, throwable)

                        metricsSender.sendReportFileNotAvailable()

                        val problem = Problem(
                            shortDescription = errorMessage,
                            context = "ReportProcessor forms fallback Error status report",
                            because = "There is not enough context here about cause",
                            possibleSolutions = listOf(
                                "MBS-11279 should help with correct error message from AdbDevice",
                                "MBS-11281 to return tests with such errors back to retry queue"
                            ),
                            throwable = throwable
                        )

                        processFailure(
                            problem = problem,
                            testStaticData = testFromSuite,
                            logcatBuffer = logcatBuffer
                        )
                    }.getOrThrow()

            is TestResult.Incomplete ->
                with(result.infraError) {

                    val problemBuilder = Problem.Builder(
                        shortDescription = "Can't complete test execution",
                        context = "ReportProcessor handling incomplete test result",
                    )

                    problemBuilder.throwable(error)

                    when (this) {
                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnParsing -> {
                            metricsSender.sendFailedOnParsingInstrumentation()
                            problemBuilder.because("Can't parse instrumentation output, see underlying exception")
                        }

                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnStart -> {
                            metricsSender.sendFailedOnStartInstrumentation()
                            problemBuilder.because("Can't start test")
                        }

                        is TestCaseRun.Result.Failed.InfrastructureError.Timeout -> {
                            metricsSender.sendTimeOut()
                            problemBuilder.because(
                                "Test didn't finish in time. " +
                                    "Test Runner has hardcoded timeout of $timeoutMin minutes"
                            )
                        }

                        is TestCaseRun.Result.Failed.InfrastructureError.Unexpected -> {
                            metricsSender.sendUnexpectedInfraError()
                        }
                    }

                    val problem = problemBuilder.build()

                    logger.warn(problem.asPlainText(), this.error)

                    processFailure(
                        problem = problem,
                        testStaticData = testFromSuite,
                        logcatBuffer = logcatBuffer
                    ).getOrThrow()
                }
        }
    }

    private fun processFailure(
        problem: Problem,
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

                val now = timeProvider.nowInSeconds()

                Result.Success(
                    AndroidTest.Lost.fromTestStaticData(
                        testStaticData,
                        startTime = now,
                        lastSignalTime = now,
                        stdout = stdout.await(),
                        stderr = stderr.await(),
                        incident = Incident(
                            type = Incident.Type.INFRASTRUCTURE_ERROR,
                            timestamp = now,
                            trace = problem.asRuntimeException().stackTraceToList(),
                            chain = listOf(
                                IncidentElement(
                                    message = problem.asPlainText()
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
