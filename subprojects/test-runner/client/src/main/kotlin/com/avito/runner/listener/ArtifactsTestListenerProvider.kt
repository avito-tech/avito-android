package com.avito.runner.listener

import com.avito.logger.LoggerFactory
import com.avito.report.Report
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.TestStaticData
import com.avito.runner.config.RunnerReportConfig
import com.avito.runner.service.listener.TestListener
import com.avito.test.model.TestCase
import java.io.File

internal class ArtifactsTestListenerProvider(
    private val loggerFactory: LoggerFactory,
    private val testRunnerOutputDir: File,
    private val testListenerFactory: TestListenerFactory,
    private val tempLogcatDir: File,
    private val report: Report,
    private val reportConfig: RunnerReportConfig.ReportViewer,
    private val proguardMappings: List<File>,
    private val saveTestArtifactsToOutputs: Boolean,
    private val macroBenchmarkOutputs: File?
) : ReportArtifactsTestListenerProvider {

    override fun provide(tests: List<TestStaticData>): TestListener {
        return ArtifactsTestListener(
            lifecycleListener = testListenerFactory.createReportTestListener(
                testStaticDataByTestCase = testStaticDataByTestCase(tests),
                tempLogcatDir = tempLogcatDir,
                report = report,
                proguardMappings = proguardMappings,
                fileStorageUrl = reportConfig.fileStorageUrl,
            ),
            outputDirectory = testRunnerOutputDir,
            loggerFactory = loggerFactory,
            saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
            reportArtifactsPullValidator = ReportAwarePullValidator(
                testArtifactsProviderFactory = TestArtifactsProviderFactory
            ),
            macrobenchmarkOutputDirectory = macroBenchmarkOutputs
        )
    }

    private fun testStaticDataByTestCase(
        testsToRun: List<TestStaticData>
    ): Map<TestCase, TestStaticData> {
        return testsToRun.associateBy { test ->
            TestCase(
                name = test.name,
                deviceName = test.device
            )
        }
    }
}
