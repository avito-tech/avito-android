package com.avito.android.runner

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.elastic.ElasticConfig
import com.avito.android.instrumentation.ActivityProvider
import com.avito.android.instrumentation.ActivityProviderFactory
import com.avito.android.log.AndroidLoggerFactory
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.runner.annotation.resolver.getTestOrThrow
import com.avito.android.runner.annotation.validation.CompositeTestMetadataValidator
import com.avito.android.runner.annotation.validation.TestMetadataValidator
import com.avito.android.runner.delegates.ReportLifecycleEventsDelegate
import com.avito.android.sentry.SentryConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.HumanReadableActionInterceptor
import com.avito.android.test.interceptor.HumanReadableAssertionInterceptor
import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.ReportFriendlyFailureHandler
import com.avito.android.test.report.ReportImplementation
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.report.ReportTestListener
import com.avito.android.test.report.ReportViewerHttpInterceptor
import com.avito.android.test.report.ReportViewerWebsocketReporter
import com.avito.android.test.report.StepDslProvider
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.screenshot.ScreenshotCapturerFactory
import com.avito.android.test.report.transport.ReportTransportFactory
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.android.test.step.StepDslDelegateImpl
import com.avito.android.util.DeviceSettingsChecker
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.TestArtifactsProviderFactory
import com.avito.report.model.Kind
import com.avito.test.http.MockDispatcher
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

abstract class InHouseInstrumentationTestRunner :
    InstrumentationTestRunner(),
    ReportProvider,
    RemoteStorageProvider {

    private val activityProvider: ActivityProvider by lazy { ActivityProviderFactory.create() }

    private val elasticConfig: ElasticConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().elasticConfig }

    private val sentryConfig: SentryConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().sentryConfig }

    private val logger by lazy { loggerFactory.create<InHouseInstrumentationTestRunner>() }

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider() }

    private val httpClientProvider: HttpClientProvider by lazy {
        HttpClientProvider(
            statsDSender = StatsDSender.Impl(
                config = testRunEnvironment.asRunEnvironmentOrThrow().statsDConfig,
                loggerFactory = loggerFactory
            ),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )
    }

    private val testArtifactsProvider: TestArtifactsProvider by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()

        TestArtifactsProviderFactory.create(
            testReportRootDir = runEnvironment.outputDirectory,
            className = runEnvironment.testMetadata.className,
            methodName = runEnvironment.testMetadata.methodName!!
        )
    }

    private val reportTransport: Transport by lazy {

        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()

        ReportTransportFactory(
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            remoteStorage = remoteStorage,
            httpClientProvider = httpClientProvider,
            testArtifactsProvider = testArtifactsProvider
        ).create(
            testRunCoordinates = runEnvironment.testRunCoordinates,
            reportDestination = runEnvironment.reportDestination
        )
    }

    @Suppress("MemberVisibilityCanBePrivate") // Public for *TestApp to skip on orchestrator runs
    val testRunEnvironment: TestRunEnvironment by lazy {
        if (isRealRun(instrumentationArguments)) {
            createRunnerEnvironment(instrumentationArguments)
        } else {
            TestRunEnvironment.OrchestratorFakeRunEnvironment
        }
    }

    @Suppress("MemberVisibilityCanBePrivate") // Public for synth monitoring
    val screenshotCapturer: ScreenshotCapturer by lazy {
        ScreenshotCapturerFactory.create(testArtifactsProvider, activityProvider)
    }

    override val loggerFactory by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        AndroidLoggerFactory(
            elasticConfig = elasticConfig,
            sentryConfig = sentryConfig,
            testName = runEnvironment.testMetadata.testName
        )
    }

    override val remoteStorage: RemoteStorage by lazy {
        RemoteStorageFactory.create(
            endpoint = testRunEnvironment.asRunEnvironmentOrThrow().fileStorageUrl,
            httpClientProvider = httpClientProvider,
            loggerFactory = loggerFactory,
            isAndroidRuntime = true
        )
    }

    override val report: InternalReport by lazy {
        ReportImplementation(
            loggerFactory = loggerFactory,
            transport = reportTransport,
            screenshotCapturer = screenshotCapturer,
            timeProvider = timeProvider,
            troubleshooter = Troubleshooter.Impl()
        )
    }

    @Suppress("unused") // used in avito
    val reportViewerHttpInterceptor: ReportViewerHttpInterceptor by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        ReportViewerHttpInterceptor(
            report = report,
            remoteFileStorageEndpointHost = runEnvironment.fileStorageUrl.host
        )
    }

    @Suppress("unused") // used in avito
    val reportViewerWebsocketReporter: ReportViewerWebsocketReporter by lazy {
        ReportViewerWebsocketReporter(report)
    }

    val mockWebServer: MockWebServer by lazy { MockWebServer() }

    val mockDispatcher by lazy { MockDispatcher(loggerFactory = loggerFactory) }

    protected abstract val metadataToBundleInjector: TestMetadataInjector

    protected open val testMetadataValidator: TestMetadataValidator =
        CompositeTestMetadataValidator(validators = emptyList())

    abstract fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment

    protected open fun beforeApplicationCreated(
        runEnvironment: TestRunEnvironment.RunEnvironment,
        bundleWithTestAnnotationValues: Bundle
    ) {
        // empty
    }

    override fun getDelegates(arguments: Bundle): List<InstrumentationTestRunnerDelegate> {
        return listOf(
            ReportLifecycleEventsDelegate(
                loggerFactory.newFactory(
                    // Because LifecycleEvents logs are needed only for test reports
                    newElasticConfig = ElasticConfig.Disabled
                )
            ),
        )
    }

    override fun beforeOnCreate(arguments: Bundle) {
        injectTestMetadata(instrumentationArguments)
        logger.debug("Instrumentation arguments: $instrumentationArguments")
        val environment = testRunEnvironment.asRunEnvironmentOrThrow()
        logger.debug("TestRunEnvironment: $environment")
        StepDslProvider.initialize(
            StepDslDelegateImpl(
                reportLifecycle = report,
                stepModelFactory = report,
            )
        )
        initApplicationCrashHandling()
        addReportListener(arguments)
        initTestCase(environment)
        initListeners(environment)
        beforeApplicationCreated(
            runEnvironment = environment,
            bundleWithTestAnnotationValues = arguments
        )
    }

    override fun afterOnCreate(arguments: Bundle) {
        Espresso.setFailureHandler(ReportFriendlyFailureHandler())
        initUITestConfig()
        DeviceSettingsChecker(
            context = targetContext,
            loggerFactory = loggerFactory
        ).check()
    }

    private fun injectTestMetadata(arguments: Bundle) {
        if (isRealRun(arguments)) {
            val test = getTest(arguments)

            metadataToBundleInjector.inject(test, arguments)
            testMetadataValidator.validate(test)
        }
    }

    private fun getTest(instrumentationArguments: Bundle): TestMethodOrClass {
        val testName = instrumentationArguments.getString("class")

        if (testName.isNullOrBlank()) {
            throw RuntimeException("Test name not found in instrumentation arguments: $instrumentationArguments")
        }
        return MethodStringRepresentation.parseString(testName).getTestOrThrow()
    }

    override fun onStart() {
        super.onStart()

        testRunEnvironment.executeIfRealRun {
            validateEnvironment(runEnvironment = it)
        }
    }

    override fun onException(obj: Any?, e: Throwable): Boolean {
        testRunEnvironment.executeIfRealRun {
            logger.warn("Application crash captured by onException handler inside instrumentation", e)
            reportUnexpectedIncident(incident = e)
        }

        return super.onException(obj, e)
    }

    @CallSuper
    @Suppress("MagicNumber")
    open fun initUITestConfig() {
        with(UITestConfig) {
            waiterTimeoutMs = TimeUnit.SECONDS.toMillis(12)

            activityLaunchTimeoutMilliseconds = TimeUnit.SECONDS.toMillis(15)

            actionInterceptors += HumanReadableActionInterceptor {
                report.addComment(it)
            }

            assertionInterceptors += HumanReadableAssertionInterceptor {
                report.addComment(it)
            }

            onWaiterRetry = { }
        }
    }

    internal fun reportUnexpectedIncident(incident: Throwable) {
        report.unexpectedFailedTestCase(incident)
    }

    /**
     * Мы перехватываем все падения приложения тут с помощью глобального хэндлера.
     * Мы используем этот механизм вместе с onException.
     *
     * Если происходит падение внутри приложения в другом треде (например в IO), то срабатывает
     * глобальный обработчик ошибок и крашит приложение внутри Android Runtime. Это падение
     * instrumentation не перехватывает.
     *
     * Сейчас за обработку всех падений приложения в mainThread и внутри instrumentation колбеков
     * отвечает onException. Все остальное (например, падение в отдельном треде) мы перехватываем в
     * глобальном обработчике.
     */
    private fun initApplicationCrashHandling() {
        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler(
            ReportUncaughtHandler(
                loggerFactory = loggerFactory,
                globalExceptionHandler = currentHandler,
                nonCriticalErrorMessages = setOf("Error while disconnecting UiAutomation")
            )
        )
    }

    private fun validateEnvironment(@Suppress("UNUSED_PARAMETER") runEnvironment: TestRunEnvironment.RunEnvironment) {
        // todo validate
    }

    private fun initTestCase(runEnvironment: TestRunEnvironment.RunEnvironment) {
        report.initTestCase(testMetadata = runEnvironment.testMetadata)
    }

    private fun initListeners(runEnvironment: TestRunEnvironment.RunEnvironment) {
        TestLifecycleNotifier.addListener(
            VideoCaptureTestListener(
                videoFeatureValue = runEnvironment.videoRecordingFeature,
                testArtifactsProvider = testArtifactsProvider,
                shouldRecord = shouldRecordVideo(runEnvironment.testMetadata),
                loggerFactory = loggerFactory,
                transport = reportTransport,
            )
        )
    }

    private fun shouldRecordVideo(testMetadata: TestMetadata): Boolean {
        return when (testMetadata.kind) {
            Kind.UI_COMPONENT, Kind.E2E -> true
            else -> false
        }
    }

    private fun addReportListener(arguments: Bundle) {
        arguments.putString("listener", ReportTestListener::class.java.name)
        arguments.putString("newRunListenerMode", "true")
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        try {
            super.finish(resultCode, results)
        } catch (e: IllegalStateException) {
            // IllegalStateException("UiAutomation not connected") occurs unrelated to our code.
            // We use uiAutomation only for VideoCapture but without capturing this exception occurs with same frequency
            if (e.message?.contains("UiAutomation not connected") != true) {
                throw e
            } else {
                logger.debug("Got UiAutomation not connected when finished")
            }
        }
    }

    companion object {
        val instance: InHouseInstrumentationTestRunner by lazy {
            InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner
        }
    }
}
