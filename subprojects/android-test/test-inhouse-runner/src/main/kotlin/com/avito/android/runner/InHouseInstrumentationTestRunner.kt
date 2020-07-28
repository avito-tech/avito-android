package com.avito.android.runner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.test.espresso.Espresso
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.monitoring.CompositeTestIssuesMonitor
import com.avito.android.monitoring.TestIssuesMonitor
import com.avito.android.monitoring.createSentry
import com.avito.android.runner.ContextFactory.Companion.FAKE_ORCHESTRATOR_RUN_ARGUMENT
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
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
import com.avito.android.test.report.performance.PerformanceProvider
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.LocalRunTransport
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.android.util.DeviceSettingsChecker
import com.avito.android.util.ImitateFlagProvider
import com.avito.logger.Logger
import com.avito.report.model.DeviceName
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.Kind
import com.avito.test.http.MockDispatcher
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

abstract class InHouseInstrumentationTestRunner :
    InstrumentationTestRunner(),
    ReportProvider,
    PerformanceProvider,
    ImitateFlagProvider {

    protected val tag = "UITestRunner"

    protected val sentry by lazy {
        createSentry(
            sentryDsn = testRunEnvironment.asRunEnvironmentOrThrow().sentryDsn
        )
    }

    private val testReportLogger: Logger = object : Logger {
        override fun debug(msg: String) {
            Log.d("TestReport", msg)
        }

        override fun info(msg: String) {
            Log.i("TestReport", msg)
        }

        override fun warn(msg: String, error: Throwable?) {
            Log.w("TestReport", msg, error)
        }

        override fun exception(msg: String, error: Throwable) {
            Log.e("TestReport", msg, error)
        }

        override fun critical(msg: String, error: Throwable) {
            Log.e("TestReport", msg, error)
            sentry.sendException(error)
        }
    }

    override val performanceTestReporter = PerformanceTestReporter()

    override val report: Report by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        val isLocalRun = runEnvironment.teamcityBuildId == TestRunEnvironment.LOCAL_STUDIO_RUN_ID
        val transport: List<Transport> = when {
            isLocalRun -> {
                if (runEnvironment.reportConfig != null) {
                    listOf(
                        LocalRunTransport(
                            reportApiHost = runEnvironment.reportConfig.reportApiUrl,
                            reportFallbackUrl = runEnvironment.reportConfig.reportApiFallbackUrl,
                            reportViewerUrl = runEnvironment.reportConfig.reportViewerUrl,
                            reportCoordinates = runEnvironment.testRunCoordinates,
                            deviceName = DeviceName(runEnvironment.deviceName),
                            logger = testReportLogger
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
                listOf(ExternalStorageTransport(gson))
            }
        }

        ReportImplementation(
            fileStorageUrl = runEnvironment.fileStorageUrl,
            onDeviceCacheDirectory = runEnvironment.outputDirectory,
            httpClient = reportHttpClient,
            onIncident = { testIssuesMonitor.onFailure(it) },
            performanceTestReporter = performanceTestReporter,
            transport = transport,
            logger = testReportLogger,
            isSyntheticStepEnabled = runEnvironment.isSyntheticStepsEnabled
        )
    }

    override val isImitate: Boolean by lazy {
        testRunEnvironment.asRunEnvironmentOrThrow().isImitation
    }

    val testRunEnvironment: TestRunEnvironment by lazy {
        createRunnerEnvironment(instrumentationArguments)
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

    @SuppressLint("LogNotTimber")
    val mockDispatcher = MockDispatcher(logger = { Log.d("MOCK_WEB_SERVER", it) })

    protected open val testIssuesMonitor: TestIssuesMonitor by lazy {
        CompositeTestIssuesMonitor(
            sentry = sentry,
            testRunEnvironment = testRunEnvironment.asRunEnvironmentOrThrow(),
            logTag = tag
        )
    }

    private lateinit var instrumentationArguments: Bundle

    private val reportHttpClient: OkHttpClient by lazy { createReportHttpClient() }

    abstract fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment

    protected abstract val metadataToBundleInjector: TestMetadataInjector

    private fun injectTestMetadata(arguments: Bundle) {
        val isRealRun = !arguments.containsKey(FAKE_ORCHESTRATOR_RUN_ARGUMENT)
        if (isRealRun) {
            metadataToBundleInjector.inject(arguments)
        }
    }

    protected open fun beforeApplicationCreated(
        runEnvironment: TestRunEnvironment.RunEnvironment,
        bundleWithTestAnnotationValues: Bundle
    ) {
    }

    /**
     * WARNING: Can't crash in this method.
     * Otherwise we can't pass an error to the report
     */
    @SuppressLint("LogNotTimber")
    override fun onCreate(arguments: Bundle) {
        instrumentationArguments = arguments
        injectTestMetadata(arguments)

        Log.d(tag, "Instrumentation arguments: $instrumentationArguments")
        Log.d(tag, "TestRunEnvironment: $testRunEnvironment")

        testRunEnvironment.executeIfRealRun {
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
            Espresso.setFailureHandler(
                ReportFriendlyFailureHandler(DefaultFailureHandler(targetContext))
            )
            initUITestConfig()

            DeviceSettingsChecker(targetContext).check()
        }
    }

    override fun createFactory(): ContextFactory {
        return object : ContextFactory.Default() {
            override fun createIfRealRun(arguments: Bundle): Context {
                return DefaultTestInstrumentationContext(
                    errorsReporter = SentryErrorsReporter(sentry)
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        testRunEnvironment.executeIfRealRun {
            validateEnvironment(runEnvironment = it)
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onException(obj: Any?, e: Throwable): Boolean {
        Log.e(tag, "Application crash captured by onException handler inside instrumentation", e)

        testRunEnvironment.executeIfRealRun {
            tryToReportUnexpectedIncident(
                incident = e,
                tag = tag
            )
        }

        return super.onException(obj, e)
    }

    @CallSuper
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

    @SuppressLint("LogNotTimber")
    fun tryToReportUnexpectedIncident(incident: Throwable, tag: String) {
        try {
            report.registerIncident(AppCrashException(incident))
            report.reportTestCase()
        } catch (t: Throwable) {
            Log.w(tag, "Can't register and report unexpected incident", t)
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
            ReportUncaughtHandler()
        )
    }

    private fun validateEnvironment(runEnvironment: TestRunEnvironment.RunEnvironment) {
        //todo validate
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
                fileStorageUrl = runEnvironment.fileStorageUrl
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
            // we made a research.
            // It showed that IllegalStateException("UiAutomation not connected") occurs unrelated to our code.
            // We use uiAutomation only for VideoCapture but without capturing this exception occurs with same frequency
            if (e.message?.contains("UiAutomation not connected") != true) {
                throw e
            } else {
                Log.d(tag, "Got UiAutomation not connected when finished")
            }
        }
    }

    companion object {
        val instance: InHouseInstrumentationTestRunner by lazy {
            InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner
        }
    }
}
