package com.avito.test.report.listener

import com.avito.android.log.AndroidTestLoggerMetadataProvider
import com.avito.android.stats.StatsDSender
import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.ReportFactory
import com.avito.android.test.report.StepDslProvider
import com.avito.android.test.report.arguments.ArgsProvider
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.android.test.step.StepDslDelegateImpl
import com.avito.android.transport.ReportTransportFactory
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.serialize.ReportSerializer
import com.avito.reportviewer.ReportViewerQuery
import com.avito.test.model.TestName
import com.avito.test.report.arguments.PropertiesArgsProvider
import com.avito.test.report.incident.AssertionBasedIncidentTypeDeterminer
import com.avito.test.report.listener.description.DescriptionMetadataParser
import com.avito.test.report.screenshot.NoOpScreenshotCapturer
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.junit.runner.Description
import org.junit.runner.notification.RunListener
import java.util.Base64

public class RobolectricReportTestListener(
    testClass: Class<*>,
    private val descriptionMetadataParser: DescriptionMetadataParser
) : RunListener() {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private val argsProvider: ArgsProvider by lazy {
        PropertiesArgsProvider(System.getProperties())
    }

    private val runEnvironment: RunEnvironment by lazy {
        parseEnvironment(argsProvider)
    }

    private val loggerFactory: LoggerFactory = LoggerFactoryBuilder()
        .metadataProvider(AndroidTestLoggerMetadataProvider(testClass::class.java.simpleName))
        .addLoggingHandlerProvider(PrintlnLoggingHandlerProvider(LogLevel.DEBUG, printStackTrace = false))
        .build()

    private val httpClientProvider: HttpClientProvider = HttpClientProvider(
        statsDSender = StatsDSender.create(
            config = runEnvironment.statsDConfig,
            loggerFactory = loggerFactory
        ),
        timeProvider = timeProvider,
        loggerFactory = loggerFactory
    )

    private val remoteStorage: RemoteStorage = RemoteStorageFactory.create(
        endpoint = runEnvironment.fileStorageUrl,
        httpClientProvider = httpClientProvider,
        isAndroidRuntime = true
    )

    private val outputDirProvider = BuildOutputDirProvider(runEnvironment.testResultsDirectory)

    private lateinit var report: InternalReport

    @Suppress("NewApi") // this is a JVM module, suppressing IDE warnings
    override fun testStarted(description: Description) {
        val metadata = descriptionMetadataParser.parse(description)

        val reportTransport = ReportTransportFactory(
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            remoteStorage = remoteStorage,
            httpClientProvider = httpClientProvider,
            testArtifactsProvider = TestArtifactsProviderFactory.createForJavaRuntime(
                provider = outputDirProvider,
                testName = metadata.testName
            ),
            reportViewerQuery = ReportViewerQuery { Base64.getEncoder().encodeToString(it) },
            reportSerializer = ReportSerializer()
        ).create(
            testRunCoordinates = runEnvironment.testRunCoordinates,
            reportDestination = parseReportDestination(
                argumentsProvider = argsProvider,
                environment = metadata.environment
            )
        )

        report = ReportFactory.createReport(
            loggerFactory = loggerFactory,
            transport = reportTransport,
            screenshotCapturer = NoOpScreenshotCapturer,
            timeProvider = timeProvider,
            incidentTypeDeterminer = AssertionBasedIncidentTypeDeterminer(),
            troubleshooter = Troubleshooter.Builder().withDefaults().build()
        )

        StepDslProvider.initialize(
            StepDslDelegateImpl(
                reportLifecycle = report,
                stepModelFactory = report,
            )
        )

        report.initTestCase(
            TestMetadata(
                caseId = null,
                description = null,
                name = TestName(metadata.className, metadata.testName),
                dataSetNumber = null,
                kind = Kind.UI_COMPONENT,
                priority = null,
                behavior = null,
                externalId = null,
                featureIds = emptyList(),
                tagIds = emptyList(),
                flakiness = Flakiness.Stable
            )
        )
        report.startTestCase()
    }

    override fun testFinished(description: Description?) {
        report.finishTestCase()
        StepDslProvider.reset()
    }
}
