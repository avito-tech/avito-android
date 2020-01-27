package com.avito.android.runner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import com.avito.android.mock.MockDispatcher
import com.avito.android.monitoring.CompositeTestIssuesMonitor
import com.avito.android.monitoring.TestIssuesMonitor
import com.avito.android.monitoring.createSentry
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.HumanReadableActionInterceptor
import com.avito.android.test.interceptor.HumanReadableAssertionInterceptor
import com.avito.android.test.report.*
import com.avito.android.test.report.incident.AppCrashException
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.TestType
import com.avito.android.test.report.performance.PerformanceProvider
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.android.util.DeviceSettingsChecker
import com.avito.android.util.ImitateFlagProvider
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

abstract class InHouseInstrumentationTestRunner() :
    AndroidJUnitRunner(),
    ReportProvider,
    PerformanceProvider,
    ImitateFlagProvider {

    protected val tag = "UITestRunner"

    protected val sentry by lazy {
        createSentry(
            sentryDsn = testRunEnvironment.asRunEnvironmentOrThrow().sentryDsn
        )
    }

    override val performanceTestReporter = PerformanceTestReporter()

    override val report: Report by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()

        ReportImplementation(
            planSlug = runEnvironment.planSlug,
            jobSlug = runEnvironment.jobSlug,
            runId = runEnvironment.runId,
            deviceName = runEnvironment.deviceName,
            isLocalRun = runEnvironment.teamcityBuildId <= 0,
            sentry = sentry,
            fileStorageUrl = runEnvironment.fileStorageUrl,
            reportApiUrl = runEnvironment.reportApiUrl,
            reportApiFallbackUrl = runEnvironment.reportApiFallbackUrl,
            reportViewerUrl = runEnvironment.reportViewerUrl,
            onDeviceCacheDirectory = runEnvironment.outputDirectory,
            httpClient = reportHttpClient,
            onIncident = { testIssuesMonitor.onFailure(it) },
            performanceTestReporter = performanceTestReporter
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
            remoteFileStorageEndpointHost = HttpUrl.get(runEnvironment.fileStorageUrl).host()
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

    private val systemDialogsManager: SystemDialogsManager by lazy { SystemDialogsManager(report) }

    private val reportHttpClient: OkHttpClient by lazy { createReportHttpClient() }

    abstract fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment

    protected abstract val metadataToBundleInjector: TestMetadataInjector

    private fun injectTestMetadata(arguments: Bundle) {
        when(TestRunEnvironment.Environment.getEnvironment(BundleArgsProvider(arguments))) {
            TestRunEnvironment.Environment.IN_HOUSE -> metadataToBundleInjector.inject(arguments)
            TestRunEnvironment.Environment.ORCHESTRATOR -> { /*do nothing*/}
        }
    }

    protected open fun beforeApplicationCreated(
        runEnvironment: TestRunEnvironment.RunEnvironment,
        bundleWithTestAnnotationValues: Bundle
    ) {
    }

    /**
     * Нельзя крешить в этом методе, иначе мы не сможем доставить ошибку до репорта
     */
    @SuppressLint("LogNotTimber")
    override fun onCreate(arguments: Bundle) {
        instrumentationArguments = arguments
        injectTestMetadata(arguments)

        Log.d(tag, testRunEnvironment.toString())

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
            Espresso.setFailureHandler(ReportFriendlyFailureHandler(targetContext))
            initUITestConfig()
        }

        DeviceSettingsChecker(targetContext).check()
    }

    override fun onStart() {
        testRunEnvironment.executeIfRealRun {
            systemDialogsManager.closeSystemDialogs()
        }

        super.onStart()

        testRunEnvironment.executeIfRealRun {
            validateEnvironment(runEnvironment = it)
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onException(
        obj: Any?,
        e: Throwable
    ): Boolean {
        Log.e(tag, "Application crash captured by onException handler inside instrumentation", e)

        testRunEnvironment.executeIfRealRun {
            tryToReportUnexpectedIncident(
                incident = e,
                tag = tag
            )
        }

        return super.onException(obj, e)
    }

    @SuppressLint("LogNotTimber")
    fun tryToReportUnexpectedIncident(incident: Throwable, tag: String) {
        try {
            report.registerIncident(AppCrashException(incident))
            report.reportTestCase()
        } catch (t: Throwable) {
            Log.e(tag, "Error during reporting test after global exception handling", t)
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
        val packageParserResult = runEnvironment.testMetadata.packageParserResult

        if (packageParserResult is TestPackageParser.Result.Error) {
            throw packageParserResult.error
        }
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
        return when (testMetadata.testType) {
            TestType.FUNCTIONAL,
            TestType.COMPONENT,
            TestType.PUBLISH,
            TestType.MESSENGER -> true

            TestType.PERFORMANCE_FUNCTIONAL,
            TestType.PERFORMANCE_COMPONENT,
            TestType.SCREENSHOT,
            TestType.MANUAL,
            TestType.UNIT,
            TestType.NONE -> false
        }
    }

    private fun initUITestConfig() {
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

            /**
             * Большинство ексепшенов от espresso оборачивается в UITestFrameworkException
             */
            waiterAllowedExceptions = waiterAllowedExceptions.plus(
                UITestFrameworkException::class.java
            )
        }
    }

    private fun addReportListener(arguments: Bundle) {
        arguments.putString("listener", ReportTestListener::class.java.name)
        arguments.putString("newRunListenerMode", "true")
    }

    companion object {
        val instance: InHouseInstrumentationTestRunner by lazy {
            InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner
        }
    }
}
