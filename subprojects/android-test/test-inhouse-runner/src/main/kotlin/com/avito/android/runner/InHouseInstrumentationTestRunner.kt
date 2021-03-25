package com.avito.android.runner

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.elastic.ElasticConfig
import com.avito.android.log.AndroidLoggerFactory
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.runner.annotation.resolver.getTestOrThrow
import com.avito.android.runner.annotation.validation.CompositeTestMetadataValidator
import com.avito.android.runner.annotation.validation.TestMetadataValidator
import com.avito.android.runner.delegates.MainLooperMessagesLogDelegate
import com.avito.android.runner.delegates.ReportLifecycleEventsDelegate
import com.avito.android.sentry.SentryConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.HumanReadableActionInterceptor
import com.avito.android.test.interceptor.HumanReadableAssertionInterceptor
import com.avito.android.test.report.Report
import com.avito.android.test.report.ReportFriendlyFailureHandler
import com.avito.android.test.report.ReportImplementation
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.report.ReportTestListener
import com.avito.android.test.report.ReportViewerHttpInterceptor
import com.avito.android.test.report.ReportViewerWebsocketReporter
import com.avito.android.test.report.incident.AppCrashException
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.LocalRunTransport
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.android.test.report.troubleshooting.dump.MainLooperMessagesLogDumper
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.android.util.DeviceSettingsChecker
import com.avito.android.util.ImitateFlagProvider
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.logger.create
import com.avito.report.ReportsApiFactory
import com.avito.report.model.DeviceName
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.Kind
import com.avito.test.http.MockDispatcher
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

abstract class InHouseInstrumentationTestRunner :
    InstrumentationTestRunner(),
    ReportProvider,
    ImitateFlagProvider,
    RemoteStorageProvider {

    private val elasticConfig: ElasticConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().elasticConfig }

    private val sentryConfig: SentryConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().sentryConfig }

    private val logger by lazy { loggerFactory.create<InHouseInstrumentationTestRunner>() }

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider() }

    private val mainLooperMessagesLogDumper by lazy { MainLooperMessagesLogDumper() }

    private val httpClientProvider: HttpClientProvider by lazy {
        HttpClientProvider(
            statsDSender = StatsDSender.Impl(
                config = testRunEnvironment.asRunEnvironmentOrThrow().statsDConfig,
                loggerFactory = loggerFactory
            ),
            timeProvider = timeProvider
        )
    }

    /**
     * Public for *TestApp to skip on orchestrator runs
     */
    val testRunEnvironment: TestRunEnvironment by lazy {
        if (isRealRun(instrumentationArguments)) {
            createRunnerEnvironment(instrumentationArguments)
        } else {
            TestRunEnvironment.OrchestratorFakeRunEnvironment
        }
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
            loggerFactory = loggerFactory,
            endpoint = testRunEnvironment.asRunEnvironmentOrThrow().fileStorageUrl,
            timeProvider = timeProvider,
            httpClientProvider = httpClientProvider
        )
    }

    override val report: Report by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        val testReportLogger = loggerFactory.create<Report>()
        val isLocalRun = runEnvironment.teamcityBuildId == TestRunEnvironment.LOCAL_STUDIO_RUN_ID
        val transport: List<Transport> = when {
            isLocalRun -> {
                val reportConfig = runEnvironment.reportConfig
                if (reportConfig != null) {
                    listOf(
                        LocalRunTransport(
                            reportViewerUrl = reportConfig.reportViewerUrl,
                            reportCoordinates = runEnvironment.testRunCoordinates,
                            deviceName = DeviceName(runEnvironment.deviceName),
                            logger = testReportLogger,
                            reportsApi = ReportsApiFactory.create(
                                host = reportConfig.reportApiUrl,
                                loggerFactory = loggerFactory,
                                httpClientProvider = httpClientProvider
                            )
                        )
                    )
                } else {
                    emptyList()
                }
            }
            else -> {
                val gson: Gson = GsonBuilder()
                    .registerTypeAdapterFactory(EntryTypeAdapterFactory())
                    .create()

                listOf(
                    ExternalStorageTransport(
                        gson = gson,
                        timeProvider = timeProvider,
                        loggerFactory = loggerFactory
                    )
                )
            }
        }

        ReportImplementation(
            onDeviceCacheDirectory = runEnvironment.outputDirectory,
            transport = transport,
            loggerFactory = loggerFactory,
            remoteStorage = remoteStorage,
            timeProvider = timeProvider,
            troubleshooter = Troubleshooter.Impl(mainLooperMessagesLogDumper)
        )
    }

    override val isImitate: Boolean by lazy {
        testRunEnvironment.asRunEnvironmentOrThrow().isImitation
    }

    @Suppress("unused") // used in avito
    val reportViewerHttpInterceptor: ReportViewerHttpInterceptor by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        ReportViewerHttpInterceptor(
            reportProvider = this,
            remoteFileStorageEndpointHost = runEnvironment.fileStorageUrl.toHttpUrl().host
        )
    }

    @Suppress("unused") // used in avito
    val reportViewerWebsocketReporter: ReportViewerWebsocketReporter by lazy {
        ReportViewerWebsocketReporter(this)
    }

    val mockWebServer: MockWebServer by lazy { MockWebServer() }

    val mockDispatcher by lazy { MockDispatcher(loggerFactory = loggerFactory) }

    private lateinit var instrumentationArguments: Bundle

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
            ReportLifecycleEventsDelegate(report),
            MainLooperMessagesLogDelegate(mainLooperMessagesLogDumper)
        )
    }

    override fun beforeOnCreate(arguments: Bundle) {
        instrumentationArguments = arguments
        injectTestMetadata(arguments)
        logger.debug("Instrumentation arguments: $instrumentationArguments")
        logger.debug("TestRunEnvironment: $testRunEnvironment")
        val environment = testRunEnvironment.asRunEnvironmentOrThrow()
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
            tryToReportUnexpectedIncident(incident = e)
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

    fun tryToReportUnexpectedIncident(incident: Throwable) {
        try {
            if (!report.isWritten) {
                report.registerIncident(AppCrashException(incident))
                report.reportTestCase()
            }
        } catch (t: Throwable) {
            logger.critical("Can't register and report unexpected incident", t)
        }
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
                onDeviceCacheDirectory = runEnvironment.outputDirectory,
                shouldRecord = shouldRecordVideo(runEnvironment.testMetadata),
                loggerFactory = loggerFactory,
                remoteStorage = remoteStorage
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
