package com.avito.instrumentation.internal.report.listener

import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun

internal class ReportProcessorImpl(
    loggerFactory: LoggerFactory,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val metricsSender: InstrumentationMetricsSender,
    private val testArtifactsProcessor: TestArtifactsProcessor
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

                        testArtifactsProcessor.processFailure(
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

                    testArtifactsProcessor.processFailure(
                        throwable = error,
                        testStaticData = testFromSuite,
                        logcatBuffer = logcatBuffer
                    ).getOrThrow()
                }
        }
    }
}
