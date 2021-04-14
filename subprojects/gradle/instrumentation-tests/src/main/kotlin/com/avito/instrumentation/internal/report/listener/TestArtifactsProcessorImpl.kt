package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.ReportFileProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.Entry
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

internal class TestArtifactsProcessorImpl(
    private val gson: Gson,
    private val testArtifactsUploader: TestArtifactsUploader,
    private val retracer: ProguardRetracer,
    private val timeProvider: TimeProvider,
    private val coroutineDispatcher: CoroutineDispatcher
) : TestArtifactsProcessor {

    override fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest> {

        val reportFileProvider = ReportFileProviderFactory.create(
            testReportRootDir = lazy { reportDir },
            testStaticData = testStaticData
        )

        val reportArtifactsUploader = ReportArtifactsUploader(
            testArtifactsUploader = testArtifactsUploader,
            reportFileProvider = reportFileProvider
        )

        // extract dispatcher
        // check if coroutine name kept
        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + coroutineDispatcher)

        return reportFileProvider.provideReportFile().map { reportJson ->

            val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                FileReader(reportJson)
            )

            runBlocking {
                withContext(scope.coroutineContext) {
                    val isTestFailed = testRuntimeData.incident != null

                    val incident = async {
                        reportArtifactsUploader.processIncident(testRuntimeData.incident)
                    }

                    val video = async {
                        reportArtifactsUploader.processVideo(testRuntimeData.video)
                    }

                    val preconditionList = async {
                        reportArtifactsUploader.processStepList(testRuntimeData.preconditions)
                    }

                    val stepList = async {
                        reportArtifactsUploader.processStepList(testRuntimeData.steps)
                    }

                    val stdout = async {
                        uploadLogcat(logcatBuffer?.getStdout(), isUploadNeeded = isTestFailed)
                    }

                    val stdErr = async {
                        uploadLogcat(logcatBuffer?.getStderr(), isUploadNeeded = isTestFailed)
                    }

                    AndroidTest.Completed.create(
                        testStaticData = testStaticData,
                        testRuntimeData = TestRuntimeDataPackage(
                            incident = incident.await(),
                            startTime = testRuntimeData.startTime,
                            endTime = testRuntimeData.endTime,
                            dataSetData = testRuntimeData.dataSetData,
                            video = video.await(),
                            preconditions = preconditionList.await(),
                            steps = stepList.await()
                        ),
                        stdout = stdout.await(),
                        stderr = stdErr.await()
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

        val scope = CoroutineScope(CoroutineName("test-artifacts-failure-${testStaticData.name}") + coroutineDispatcher)

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
                testArtifactsUploader.upload(
                    content = retracer.retrace(logcat.joinToString(separator = "\n")),
                    type = Entry.File.Type.plain_text
                )
                    .fold(
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
