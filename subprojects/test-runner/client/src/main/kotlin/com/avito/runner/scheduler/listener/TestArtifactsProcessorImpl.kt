package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.report.serialize.ReportSerializer
import com.avito.runner.scheduler.logcat.LogcatAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

internal class TestArtifactsProcessorImpl(
    private val reportSerializer: ReportSerializer,
    private val testArtifactsUploader: TestArtifactsUploader,
    private val dispatcher: CoroutineDispatcher,
    private val logcatProcessor: LogcatProcessor
) : TestArtifactsProcessor {

    override fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatAccessor: LogcatAccessor
    ): Result<AndroidTest> {

        val reportFileProvider = TestArtifactsProviderFactory.createForTempDir(reportDir)

        val reportArtifactsUploader = ReportArtifactsUploader(
            testArtifactsUploader = testArtifactsUploader,
            testArtifactsProvider = reportFileProvider
        )

        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + dispatcher)

        return reportFileProvider.provideReportFile().flatMap { reportJson ->
            reportSerializer.deserialize(reportJson)
        }.map { testRuntimeData ->

            runBlocking {
                withContext(scope.coroutineContext) {
                    val isTestFailed = testRuntimeData.incident != null

                    val incident = async {
                        reportArtifactsUploader.processIncident(testRuntimeData.incident)
                    }

                    val video = async {
                        reportArtifactsUploader.processVideo(testRuntimeData.video)
                    }

                    val preconditionList = reportArtifactsUploader.processStepList(testRuntimeData.preconditions)

                    val stepList = reportArtifactsUploader.processStepList(testRuntimeData.steps)

                    val logcat = async {
                        logcatProcessor.process(logcatAccessor, isUploadNeeded = isTestFailed)
                    }

                    AndroidTest.Completed.create(
                        testStaticData = testStaticData,
                        testRuntimeData = TestRuntimeDataPackage(
                            incident = incident.await(),
                            startTime = testRuntimeData.startTime,
                            endTime = testRuntimeData.endTime,
                            dataSetData = testRuntimeData.dataSetData,
                            video = video.await(),
                            preconditions = preconditionList.map { it.await() },
                            steps = stepList.map { it.await() }
                        ),
                        logcat = logcat.await(),
                    )
                }
            }
        }
    }
}
