package com.avito.instrumentation.internal.report.listener

import com.avito.android.runner.report.Report
import com.avito.filestorage.HttpRemoteStorage
import com.avito.filestorage.RemoteStorage
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.AndroidTest
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.retrace.ProguardRetracer
import com.avito.runner.scheduler.listener.TestLifecycleListener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError.FailedOnParsing
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError.FailedOnStart
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError.Timeout
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError.Unexpected
import com.avito.runner.service.worker.device.Device
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader

internal class ReportViewerTestReporter(
    loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val report: Report,
    private val remoteStorage: RemoteStorage,
    private val logcatDir: File,
    private val retracer: ProguardRetracer,
    private val metricsSender: InstrumentationMetricsSender
) : TestReporter() {

    private val logger = loggerFactory.create<ReportViewerTestReporter>()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    private val logcatBuffers = mutableMapOf<Pair<TestCase, Int>, LogcatBuffer>()

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
        super.started(test, device, executionNumber)

        val logcatFile = File(logcatDir, "${device.coordinate.serial}.txt")

        val key = test to executionNumber
        logcatBuffers[key] = LogcatBuffer.Impl(
            logcatFile = logcatFile
        )
    }

    override fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        super.finished(result, test, executionNumber)
        val key = test to executionNumber
        logcatBuffers.remove(key)?.stop()
    }

    override fun report(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        val testFromSuite = requireNotNull(testSuite[test]) { "Can't find test in suite: ${test.testName}" }
        val key = test to executionNumber

        when (result) {
            is TestResult.Complete -> {
                result.artifacts.fold(
                    { reportFile ->
                        val reportJson = File(reportFile, REPORT_JSON_ARTIFACT)

                        try {
                            val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                                FileReader(reportJson)
                            )

                            // send only for failed tests
                            val (stdout: String, stderr: String) = if (testRuntimeData.incident != null) {
                                logcatBuffers.getLogcat(key)
                            } else {
                                "" to ""
                            }

                            report.sendCompletedTest(
                                AndroidTest.Completed.create(
                                    testStaticData = testFromSuite,
                                    testRuntimeData = testRuntimeData,
                                    stdout = stdout,
                                    stderr = stderr
                                )
                            )
                        } catch (throwable: Throwable) {
                            val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                            val errorMessage = "Can't parse testRuntimeData: ${test.testName}; ${reportJson.readText()}"

                            logger.warn(errorMessage, throwable)

                            sendLostTest(
                                testFromSuite = testFromSuite,
                                errorMessage = errorMessage,
                                stdout = stdout,
                                stderr = stderr,
                                throwable = throwable
                            )

                            metricsSender.sendReportFileParseErrors()
                        }
                    },
                    { throwable ->
                        val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                        val errorMessage = "Can't get report from file: $test"

                        logger.warn(errorMessage, throwable)

                        sendLostTest(
                            testFromSuite = testFromSuite,
                            errorMessage = errorMessage,
                            stdout = stdout,
                            stderr = stderr,
                            throwable = throwable
                        )

                        metricsSender.sendReportFileNotAvailable()
                    }
                )
            }
            is TestResult.Incomplete -> {
                val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                with(result.infraError) {
                    logger.warn("${error.message} while executing ${test.testName}", this.error)

                    sendLostTest(
                        testFromSuite = testFromSuite,
                        errorMessage = error.message ?: "Empty message",
                        stdout = stdout,
                        stderr = stderr,
                        throwable = error
                    )
                    when (this) {
                        is FailedOnParsing -> metricsSender.sendFailedOnParsingInstrumentation()
                        is FailedOnStart -> metricsSender.sendFailedOnStartInstrumentation()
                        is Timeout -> metricsSender.sendTimeOut()
                        is Unexpected -> metricsSender.sendUnexpectedInfraError()
                    }
                }
            }
        }
    }

    private fun sendLostTest(
        testFromSuite: TestStaticData,
        errorMessage: String,
        stdout: String,
        stderr: String,
        throwable: Throwable
    ) {
        report.sendLostTests(
            listOf(
                AndroidTest.Lost.fromTestMetadata(
                    testFromSuite,
                    startTime = 0,
                    lastSignalTime = 0,
                    stdout = stdout,
                    stderr = stderr,
                    incident = Incident(
                        type = Incident.Type.INFRASTRUCTURE_ERROR,
                        timestamp = timeProvider.nowInSeconds(),
                        trace = throwable.stackTraceToList(),
                        chain = listOf(
                            IncidentElement(
                                message = errorMessage
                            )
                        ),
                        entryList = emptyList()
                    )
                )
            )
        )
    }

    private fun Map<Pair<TestCase, Int>, LogcatBuffer>.getLogcat(test: Pair<TestCase, Int>): Pair<String, String> {
        val logcatBuffer = get(test)
        return if (logcatBuffer != null) {
            logcatBuffer
                .getLogs()
                .let { (stdout, stderr) -> uploadLogcat(stdout) to uploadLogcat(stderr) }
        } else {
            logger.critical("Can't find logBuffer", IllegalStateException("No logBuffer by key:$test"))
            return "" to ""
        }
    }

    // todo coroutine
    private fun uploadLogcat(logcat: List<String>): String {
        return if (logcat.isEmpty()) {
            logger.warn("Logcat is empty")
            ""
        } else {
            when (val result = remoteStorage.upload(
                RemoteStorage.Request.ContentRequest.AnyContent(
                    content = retracer.retrace(logcat.joinToString(separator = "\n")),
                    extension = "log"
                ),
                comment = "logcat"
            ).get()) {
                is RemoteStorage.Result.Success -> remoteStorageFullUrl(result)
                is RemoteStorage.Result.Error -> "Failed to upload logcat: ${result.t.message}"
            }
        }
    }

    private fun remoteStorageFullUrl(result: RemoteStorage.Result.Success): String {
        check(remoteStorage is HttpRemoteStorage) // TODO: extract to interface
        return remoteStorage.fullUrl(result)
    }

    companion object {
        // todo should be passed with instrumentation params, see [ExternalStorageTransport]
        private const val REPORT_JSON_ARTIFACT = "report.json"
    }
}
