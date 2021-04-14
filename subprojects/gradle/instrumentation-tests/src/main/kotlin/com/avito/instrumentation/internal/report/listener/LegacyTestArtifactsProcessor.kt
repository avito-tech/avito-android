package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.ReportFileProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.retrace.ProguardRetracer
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

internal class LegacyTestArtifactsProcessor(
    private val gson: Gson,
    private val testArtifactsUploader: TestArtifactsUploader,
    private val retracer: ProguardRetracer,
    private val timeProvider: TimeProvider
) : TestArtifactsProcessor {

    override fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest> {

        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + Dispatchers.IO)

        val reportFileProvider = ReportFileProviderFactory.create(
            testReportRootDir = lazy { reportDir },
            testStaticData = testStaticData
        )

        val reportJson = reportFileProvider.provideReportFile()

        return Result.tryCatch {
            val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                FileReader(reportJson)
            )

            val isTestFailed = testRuntimeData.incident != null

            runBlocking {
                withContext(scope.coroutineContext) {
                    AndroidTest.Completed.create(
                        testStaticData = testStaticData,
                        testRuntimeData = testRuntimeData,
                        stdout = uploadLogcat(logcatBuffer?.getStdout(), isUploadNeeded = isTestFailed),
                        stderr = uploadLogcat(logcatBuffer?.getStderr(), isUploadNeeded = isTestFailed)
                    )
                }
            }
        }
    }

    override fun processFailure(
        throwable: Throwable,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest> {

        val scope = CoroutineScope(CoroutineName("test-artifacts-failure-${testStaticData.name}") + Dispatchers.IO)

        return runBlocking {
            withContext(scope.coroutineContext) {
                Result.Success(
                    AndroidTest.Lost.fromTestMetadata(
                        testStaticData,
                        startTime = 0,
                        lastSignalTime = 0,
                        stdout = uploadLogcat(logcatBuffer?.getStdout(), isUploadNeeded = true),
                        stderr = uploadLogcat(logcatBuffer?.getStderr(), isUploadNeeded = true),
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

    private suspend fun uploadLogcat(logcat: List<String>?, isUploadNeeded: Boolean): String {
        return if (isUploadNeeded) {
            if (logcat != null) {
                testArtifactsUploader.uploadLogcat(retracer.retrace(logcat.joinToString(separator = "\n"))).fold(
                    onSuccess = { it.toString() },
                    onFailure = { "Can't upload logcat: ${it.message}" }
                )
            } else {
                "logcat not available"
            }
        } else {
            "logcat not uploaded"
        }
    }
}
