package com.avito.android.runner

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import com.avito.android.mock.MockDispatcher
import com.avito.android.monitoring.TestIssuesMonitor
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.delegates.CompositeInstrumentationDelegate
import com.avito.android.test.report.Report
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.report.ReportViewerHttpInterceptor
import com.avito.android.test.report.ReportViewerWebsocketReporter
import com.avito.android.test.report.incident.AppCrashException
import com.avito.android.test.report.performance.PerformanceProvider
import com.avito.android.util.ImitateFlagProvider
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import java.net.IDN

abstract class InHouseInstrumentationTestRunner :
    AndroidJUnitRunner,
    ReportProvider,
    PerformanceProvider,
    ImitateFlagProvider {

    private val delegateProviders: List<InstrumentationDelegateProvider>

    constructor() : this(emptyList())

    constructor(delegateProviders: List<InstrumentationDelegateProvider>) {
        this.delegateProviders = delegateProviders
    }

    @Deprecated("")
    protected val tag = "UITestRunner"

    private val instrumentationContext: InstrumentationDelegateProvider.Context by lazy {
        InstrumentationDelegateProvider.Context(
            targetContext = targetContext,
            environment = testRunEnvironment.asRunEnvironmentOrThrow(),
            tag = tag
        )
    }

    private var mainInstrumentationDelegate: InstrumentationDelegate? = null

    @Deprecated("")
    protected val sentry by lazy {
        instrumentationContext.sentry
    }

    @Deprecated("")
    override val performanceTestReporter by lazy {
        instrumentationContext.performanceTestReporter
    }

    @Deprecated("")
    override val report: Report by lazy {
        instrumentationContext.report
    }

    @Deprecated("")
    override val isImitate: Boolean by lazy {
        testRunEnvironment.asRunEnvironmentOrThrow().isImitation
    }

    @Deprecated("")
    val testRunEnvironment: TestRunEnvironment by lazy {
        createRunnerEnvironment(instrumentationArguments)
    }

    @Deprecated("")
    val reportViewerHttpInterceptor: ReportViewerHttpInterceptor by lazy {
        val runEnvironment = testRunEnvironment.asRunEnvironmentOrThrow()
        ReportViewerHttpInterceptor(
            reportProvider = this,
            remoteFileStorageEndpointHost = HttpUrl.get(runEnvironment.fileStorageUrl).host()
        )
    }

    @Deprecated("")
    val reportViewerWebsocketReporter: ReportViewerWebsocketReporter by lazy {
        ReportViewerWebsocketReporter(this)
    }

    @Deprecated("")
    val mockWebServer: MockWebServer by lazy { MockWebServer() }

    @SuppressLint("LogNotTimber")
    @Deprecated("")
    val mockDispatcher = MockDispatcher(logger = { Log.d("MOCK_WEB_SERVER", it) })

    @Deprecated("")
    protected open val testIssuesMonitor: TestIssuesMonitor by lazy {
        instrumentationContext.testIssuesMonitor
    }

    private lateinit var instrumentationArguments: Bundle

    abstract fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment

    protected abstract val metadataToBundleInjector: TestMetadataInjector

    private fun injectTestMetadata(arguments: Bundle) {
        val isRealRun = !arguments.containsKey(FAKE_ORCHESTRATOR_RUN_ARGUMENT)
        if (isRealRun) {
            metadataToBundleInjector.inject(arguments)
        }
    }

    @Deprecated("")
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
        injectTestMetadata(arguments) // todo to delegate
        Log.d(tag, "Instrumentation arguments: $instrumentationArguments")
        Log.d(tag, "TestRunEnvironment: $testRunEnvironment")

        testRunEnvironment.executeIfRealRun {
            mainInstrumentationDelegate = CompositeInstrumentationDelegate(
                delegates = delegateProviders.map { it.get(instrumentationContext) }
            )
            beforeApplicationCreated(
                runEnvironment = it,
                bundleWithTestAnnotationValues = arguments
            )
        }
        mainInstrumentationDelegate?.beforeOnCreate(arguments)
        super.onCreate(arguments)
        mainInstrumentationDelegate?.afterOnCreate()
    }

    override fun onStart() {
        mainInstrumentationDelegate?.beforeOnStart()
        super.onStart()
        mainInstrumentationDelegate?.afterOnStart()
    }

    override fun onException(obj: Any?, e: Throwable): Boolean {
        mainInstrumentationDelegate?.onException(obj, e)
        return super.onException(obj, e)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        mainInstrumentationDelegate?.onFinish(resultCode, results)
        super.finish(resultCode, results)
    }

    @SuppressLint("LogNotTimber")
    @Deprecated("")
    fun tryToReportUnexpectedIncident(incident: Throwable, tag: String) {
        try {
            report.registerIncident(AppCrashException(incident))
            report.reportTestCase()
        } catch (t: Throwable) {
            Log.e(tag, "Error during reporting test after global exception handling", t)
        }
    }

    companion object {
        @Deprecated("")
        val instance: InHouseInstrumentationTestRunner by lazy {
            InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner
        }
    }
}
