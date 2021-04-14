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

internal class LegacyTestArtifactsProcessor(
    private val gson: Gson,
    private val logcatProcessor: LogcatProcessor,
    private val dispatcher: CoroutineDispatcher
) : TestArtifactsProcessor {

    override fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest> {

        val scope = CoroutineScope(CoroutineName("test-artifacts-${testStaticData.name}") + dispatcher)

        val reportFileProvider = ReportFileProviderFactory.create(
            testReportRootDir = lazy { reportDir },
            testStaticData = testStaticData
        )

        return reportFileProvider.provideReportFile().map { reportJson ->
            val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                FileReader(reportJson)
            )

            val isTestFailed = testRuntimeData.incident != null

            runBlocking {
                withContext(scope.coroutineContext) {

                    val stdout = async {
                        logcatProcessor.process(logcatBuffer?.getStdout(), isUploadNeeded = isTestFailed)
                    }

                    val stderr = async {
                        logcatProcessor.process(logcatBuffer?.getStderr(), isUploadNeeded = isTestFailed)
                    }

                    AndroidTest.Completed.create(
                        testStaticData = testStaticData,
                        testRuntimeData = testRuntimeData,
                        stdout = stdout.await(),
                        stderr = stderr.await()
                    )
                }
            }
        }
    }
}
