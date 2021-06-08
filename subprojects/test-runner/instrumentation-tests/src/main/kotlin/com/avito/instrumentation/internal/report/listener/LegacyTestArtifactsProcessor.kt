package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.instrumentation.internal.logcat.LogcatAccessor
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.report.serialize.ReportSerializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

internal class LegacyTestArtifactsProcessor(
    private val reportSerializer: ReportSerializer,
    private val logcatProcessor: LogcatProcessor,
    private val dispatcher: CoroutineDispatcher
) : TestArtifactsProcessor {

    override fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatAccessor: LogcatAccessor
    ): Result<AndroidTest> {

        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + dispatcher)

        val reportFileProvider = TestArtifactsProviderFactory.createForTempDir(reportDir)

        return reportFileProvider.provideReportFile()
            .flatMap { reportJson -> reportSerializer.deserialize(reportJson) }
            .map { testRuntimeData ->

                val isTestFailed = testRuntimeData.incident != null

                runBlocking {
                    withContext(scope.coroutineContext) {

                        val logcat = async {
                            logcatProcessor.process(logcatAccessor, isUploadNeeded = isTestFailed)
                        }

                        AndroidTest.Completed.create(
                            testStaticData = testStaticData,
                            testRuntimeData = testRuntimeData,
                            logcat = logcat.await()
                        )
                    }
                }
            }
    }
}
