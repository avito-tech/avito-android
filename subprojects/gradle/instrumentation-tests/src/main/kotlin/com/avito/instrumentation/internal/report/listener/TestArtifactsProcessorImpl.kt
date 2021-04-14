package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.ReportFileProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
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
    private val dispatcher: CoroutineDispatcher,
    private val logcatProcessor: LogcatProcessor
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

        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + dispatcher)

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
                        logcatProcessor.uploadLogcat(logcatBuffer?.getStdout(), isUploadNeeded = isTestFailed)
                    }

                    val stdErr = async {
                        logcatProcessor.uploadLogcat(logcatBuffer?.getStderr(), isUploadNeeded = isTestFailed)
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
}
