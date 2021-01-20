package com.avito.android.runner

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.elastic.ElasticConfig
import com.avito.android.log.AndroidLoggerFactory
import com.avito.android.monitoring.CompositeTestIssuesMonitor
import com.avito.android.monitoring.TestIssuesMonitor
import com.avito.android.runner.ContextFactory.Companion.FAKE_ORCHESTRATOR_RUN_ARGUMENT
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.runner.annotation.resolver.getTestOrThrow
import com.avito.android.runner.annotation.validation.CompositeTestMetadataValidator
import com.avito.android.runner.annotation.validation.TestMetadataValidator
import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryClient
import com.avito.android.stats.StatsDConfig
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
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.android.util.DeviceSettingsChecker
import com.avito.android.util.ImitateFlagProvider
import com.avito.filestorage.RemoteStorage
import com.avito.logger.create
import com.avito.report.ReportsApi
import com.avito.report.model.DeviceName
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.Kind
import com.avito.test.http.MockDispatcher
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.sentry.SentryClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

abstract class InHouseInstrumentationTestRunner :
    InstrumentationTestRunner(),
    ReportProvider,
    ImitateFlagProvider,
    RemoteStorageProvider {

    private val elasticConfig: ElasticConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().elasticConfig }

    private val sentryConfig: SentryConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().sentryConfig }

    private val statsDConfig: StatsDConfig by lazy { testRunEnvironment.asRunEnvironmentOrThrow().statsDConfig }

    private val logger by lazy { loggerFactory.create<InHouseInstrumentationTestRunner>() }

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider() }

    val sentryClient: SentryClient by lazy { sentryClient(config = sentryConfig) }

    val statsDSender: StatsDSender by lazy { StatsDSender.Impl(statsDConfig, loggerFactory) }

    /**
     * Public for *TestApp to skip on orchestrator runs
     */
    val testRunEnvironment: TestRunEnvironment by lazy { createRunnerEnvironment(instrumentationArguments) }

    override val loggerFactory by lazy {
        AndroidLoggerFactory(
            elasticConfig = elasticConfig,
            sentryConfig = sentryConfig
        )
    }

    override val remoteStorage: RemoteStorage by lazy {
        RemoteStorage.create(
            loggerFactory = loggerFactory,
            httpClient = reportHttpClient,
            endpoint = testRunEnvironment.asRunEnvironmentOrThrow().fileStorageUrl,
            timeProvider = timeProvider
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
                            reportsApi = ReportsApi.create(
                                host = reportConfig.reportApiUrl,
                                fallbackUrl = reportConfig.reportApiFallbackUrl,
                                readTimeout = 10,
                                writeTimeout = 10,
                                loggerFactory = loggerFactory
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
                listOf(ExternalStorageTransport(gson, loggerFactory))
            }
        }

        ReportImplementation(
            onDeviceCacheDirectory = runEnvironment.outputDirectory,
            onIncident = { testIssuesMonitor.onFailure(it) },
            transport = transport,
            loggerFactory = loggerFactory,
            remoteStorage = remoteStorage,
            timeProvider = timeProvider
        )
    }

    override val isImitate: Boolean by lazy {
        testRunEnvironment.asRunEnvironmentOrThrow().isImitation
    }

    val reportViewerHttpInterceptor: ReportViewerHttpInterceptor by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        ReportViewerHttpInterceptor(
            reportProvider = this,
            remoteFileStorageEndpointHost = runEnvironment.fileStorageUrl.toHttpUrl().host
        )
    }

    val reportViewerWebsocketReporter: ReportViewerWebsocketReporter by lazy {
        ReportViewerWebsocketReporter(this)
    }

    val mockWebServer: MockWebServer by lazy { MockWebServer() }

    val mockDispatcher by lazy { MockDispatcher(loggerFactory = loggerFactory) }

    protected open val testIssuesMonitor: TestIssuesMonitor by lazy {
        CompositeTestIssuesMonitor(
            sentry = sentryClient,
            testRunEnvironment = testRunEnvironment.asRunEnvironmentOrThrow(),
            logger = logger
        )
    }

    private lateinit var instrumentationArguments: Bundle

    private val reportHttpClient: OkHttpClient by lazy {
        createReportHttpClient(loggerFactory.create("ReportViewerHttp"))
    }

    protected abstract val metadataToBundleInjector: TestMetadataInjector

    protected open val testMetadataValidator: TestMetadataValidator =
        CompositeTestMetadataValidator(validators = emptyList())

    abstract fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment

    protected open fun beforeApplicationCreated(
        runEnvironment: TestRunEnvironment.RunEnvironment,
        bundleWithTestAnnotationValues: Bundle
    ) {
    }

    /**
     * WARNING: Shouldn't crash in this method.
     * Otherwise we can't pass an error to the report
     */
    override fun onCreate(arguments: Bundle) {
        instrumentationArguments = arguments
        injectTestMetadata(arguments)

        testRunEnvironment.executeIfRealRun {
            logger.debug("Instrumentation arguments: $instrumentationArguments")
            logger.debug("TestRunEnvironment: $testRunEnvironment")

            initApplicationCrashHandling()

            addReportListener(arguments)
            initTestCase(runEnvironment = it)
            initListeners(runEnvironment = it)
            beforeApplicationCreated(
                runEnvironment = it,
                bundleWithTestAnnotationValues = arguments
            )
        }

        super.onCreate(arguments)

        testRunEnvironment.executeIfRealRun {
            Espresso.setFailureHandler(ReportFriendlyFailureHandler())
            initUITestConfig()
            DeviceSettingsChecker(
                context = targetContext,
                loggerFactory = loggerFactory
            ).check()
        }
    }

    private fun injectTestMetadata(arguments: Bundle) {
        val isRealRun = !arguments.containsKey(FAKE_ORCHESTRATOR_RUN_ARGUMENT)
        if (isRealRun) {
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

    override fun createFactory(): ContextFactory = object : ContextFactory.Default() {

        override fun createIfRealRun(arguments: Bundle): Context = DefaultTestInstrumentationContext(
            errorsReporter = SentryErrorsReporter(sentryClient)
        )
    }

    override fun onStart() {
        super.onStart()

        testRunEnvironment.executeIfRealRun {
            validateEnvironment(runEnvironment = it)
        }
    }

    override fun onException(obj: Any?, e: Throwable): Boolean {
        logger.critical("Application crash captured by onException handler inside instrumentation", e)

        testRunEnvironment.executeIfRealRun {
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
            report.registerIncident(AppCrashException(incident))
            report.reportTestCase()
        } catch (t: Throwable) {
            logger.warn("Can't register and report unexpected incident", t)
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
        Thread.setDefaultUncaughtExceptionHandler(
            ReportUncaughtHandler(loggerFactory)
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
                httpClient = reportHttpClient,
                shouldRecord = shouldRecordVideo(runEnvironment.testMetadata),
                fileStorageUrl = runEnvironment.fileStorageUrl,
                loggerFactory = loggerFactory,
                timeProvider = timeProvider
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
