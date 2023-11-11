package com.avito.runner.listener

import com.avito.filestorage.RemoteStorageFactory
import com.avito.logger.LoggerFactory
import com.avito.report.Report
import com.avito.report.model.TestStaticData
import com.avito.report.serialize.ReportSerializer
import com.avito.retrace.ProguardRetracer
import com.avito.runner.artifacts.AvitoFileStorageUploader
import com.avito.runner.artifacts.LegacyTestArtifactsProcessor
import com.avito.runner.artifacts.TestArtifactsProcessor
import com.avito.runner.artifacts.TestArtifactsUploader
import com.avito.runner.logcat.LogcatProcessor
import com.avito.runner.report.ReportProcessor
import com.avito.runner.report.ReportProcessorImpl
import com.avito.test.model.TestCase
import com.avito.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.File

public class TestListenerFactory(
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val httpClientBuilder: OkHttpClient.Builder
) {

    public fun createReportTestListener(
        testStaticDataByTestCase: Map<TestCase, TestStaticData>,
        tempLogcatDir: File,
        report: Report,
        proguardMappings: List<File>,
        fileStorageUrl: String,
    ): TestLifecycleListener {
        return ReportTestListener(
            logcatDir = tempLogcatDir,
            reportProcessor = createReportProcessor(
                testSuite = testStaticDataByTestCase,
                proguardMappings = proguardMappings,
                fileStorageUrl = fileStorageUrl,
            ),
            report = report,
        )
    }

    private fun createReportProcessor(
        testSuite: Map<TestCase, TestStaticData>,
        proguardMappings: List<File>,
        fileStorageUrl: String,
    ): ReportProcessor {

        val dispatcher = Dispatchers.IO

        val reTracer: ProguardRetracer = ProguardRetracer.create(proguardMappings)

        val artifactsUploader: TestArtifactsUploader = AvitoFileStorageUploader(
            RemoteStorageFactory.create(
                endpoint = fileStorageUrl.toHttpUrl(),
                builder = httpClientBuilder,
                isAndroidRuntime = false
            )
        )

        val logcatUploader = LogcatProcessor.Impl(
            testArtifactsUploader = artifactsUploader,
            retracer = reTracer
        )

        return ReportProcessorImpl(
            loggerFactory = loggerFactory,
            testSuite = testSuite,
            testArtifactsProcessor = createTestArtifactsProcessor(
                reportSerializer = ReportSerializer(),
                dispatcher = dispatcher,
                logcatProcessor = logcatUploader,
            ),
            logcatProcessor = logcatUploader,
            timeProvider = timeProvider,
            dispatcher = dispatcher
        )
    }

    private fun createTestArtifactsProcessor(
        reportSerializer: ReportSerializer,
        dispatcher: CoroutineDispatcher,
        logcatProcessor: LogcatProcessor,
    ): TestArtifactsProcessor = LegacyTestArtifactsProcessor(
        reportSerializer = reportSerializer,
        logcatProcessor = logcatProcessor,
        dispatcher = dispatcher
    )
}
