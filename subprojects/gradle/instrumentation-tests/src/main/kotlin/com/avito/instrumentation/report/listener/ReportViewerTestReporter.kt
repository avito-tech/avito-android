package com.avito.instrumentation.report.listener

import com.avito.filestorage.HttpRemoteStorage
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorage.Request
import com.avito.filestorage.RemoteStorage.Result
import com.avito.instrumentation.report.Report
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
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.funktionale.tries.Try
import java.io.File
import java.io.FileReader

class ReportViewerTestReporter(
    loggerFactory: LoggerFactory,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val report: Report,
    // todo extract write to file
    fileStorageUrl: String,
    private val logcatDir: File,
    private val retracer: ProguardRetracer
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
        writeTimeoutSec = httpTimeoutSec
    )

    /**
     * todo need implementation for in memory report
     */
    private val remoteStorage: RemoteStorage =
        RemoteStorage.create(
            endpoint = fileStorageUrl,
            loggerFactory = loggerFactory,
            httpClient = httpClient
        )

    // todo переместить ближе к DeviceWorker
    // сюда можно передавать логи как параметр и убрать отсюда все кроме транспорта
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
                    val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(FileReader(reportJson))

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
                    logger.critical("Can't parse testRuntimeData: ${test.testName}; ${reportJson.readText()}", e)
                }
            },
            {
                val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                logger.critical("Can't get report from file: $test", it)
                report.sendLostTests(
                    listOf(
                        AndroidTest.Lost.fromTestMetadata(
                            testFromSuite,
                            startTime = 0, // todo попробовать достать
                            lastSignalTime = 0, // todo попробовать достать
                            stdout = stdout,
                            stderr = stderr
                        )
                    )
                )
            }
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
                Request.ContentRequest(
                    content = retracer.retrace(logcat.joinToString(separator = "\n")),
                    extension = "log"
                ),
                comment = "logcat"
            ).get()) {
                is Result.Success -> remoteStorage.fullUrl(result)
                is Result.Error -> "Failed to upload logcat: ${result.t.message}"
            }
        }
    }

    private fun RemoteStorage.fullUrl(result: Result.Success): String {
        check(remoteStorage is HttpRemoteStorage) // TODO: extract to interface
        return remoteStorage.fullUrl(result)
    }

    companion object {
        private const val REPORT_JSON_ARTIFACT = "report.json"
    }
}
