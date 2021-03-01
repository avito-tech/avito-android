package com.avito.instrumentation.internal.report.listener

import com.avito.android.runner.report.Report
import com.avito.filestorage.HttpRemoteStorage
import com.avito.filestorage.RemoteStorage
import com.avito.http.RetryInterceptor
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.internal.getHttpClient
import com.avito.report.model.AndroidTest
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.retrace.ProguardRetracer
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import com.avito.time.TimeProvider
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.funktionale.tries.Try
import java.io.File
import java.io.FileReader

internal class ReportViewerTestReporter(
    loggerFactory: LoggerFactory,
    timeProvider: TimeProvider,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val report: Report,
    // todo extract write to file
    fileStorageUrl: String,
    private val logcatDir: File,
    private val retracer: ProguardRetracer,
    private val metricsSender: InstrumentationMetricsSender
) : TestReporter() {

    private val logger = loggerFactory.create<ReportViewerTestReporter>()

    private val httpTimeoutSec = 30L

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    private val httpClient = getHttpClient(
        verbose = false, // do not enable for production, generates a ton of logs
        logger = logger,
        readTimeoutSec = httpTimeoutSec,
        writeTimeoutSec = httpTimeoutSec,
        retryInterceptor = RetryInterceptor(
            logger = logger,
            allowedMethods = listOf("POST")
        )
    )

    /**
     * todo need implementation for in memory report
     */
    private val remoteStorage: RemoteStorage =
        RemoteStorage.create(
            endpoint = fileStorageUrl,
            loggerFactory = loggerFactory,
            httpClient = httpClient,
            timeProvider = timeProvider
        )

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
        artifacts: Try<File>,
        test: TestCase,
        executionNumber: Int
    ) {
        super.finished(artifacts, test, executionNumber)
        val key = test to executionNumber
        logcatBuffers.remove(key)?.stop()
    }

    override fun report(
        artifacts: Try<File>,
        test: TestCase,
        executionNumber: Int
    ) {

        val testFromSuite = requireNotNull(testSuite[test]) { "Can't find test in suite: ${test.testName}" }

        val key = test to executionNumber
        artifacts.fold(
            { reportFile ->
                val reportJson = File(reportFile, REPORT_JSON_ARTIFACT)

                try {
                    val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                        FileReader(reportJson)
                    )

                    // отправляем только для упавших тестов
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
                } catch (e: Throwable) {
                    val errorMessage = "Can't parse testRuntimeData: ${test.testName}; ${reportJson.readText()}"
                    logger.warn(errorMessage, e)
                    sendLostTest(testFromSuite, "", errorMessage)
                    metricsSender.sendReportFileParseErrors()
                }
            },
            { throwable ->
                val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                logger.warn("Can't get report from file: $test", throwable)
                sendLostTest(testFromSuite, stdout, stderr)
                metricsSender.sendReportFileNotAvailable()
            }
        )
    }

    private fun sendLostTest(testFromSuite: TestStaticData, stdout: String, stderr: String) {
        report.sendLostTests(
            listOf(
                AndroidTest.Lost.fromTestMetadata(
                    testFromSuite,
                    startTime = 0,
                    lastSignalTime = 0,
                    stdout = stdout,
                    stderr = stderr
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
                RemoteStorage.Request.ContentRequest(
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
