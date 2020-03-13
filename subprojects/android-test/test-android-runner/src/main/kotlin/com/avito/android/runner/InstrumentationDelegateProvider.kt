package com.avito.android.runner

import android.content.Context
import android.util.Log
import com.avito.android.monitoring.CompositeTestIssuesMonitor
import com.avito.android.monitoring.TestIssuesMonitor
import com.avito.android.monitoring.createSentry
import com.avito.android.test.report.Report
import com.avito.android.test.report.ReportImplementation
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.LocalRunTransport
import com.avito.android.test.report.transport.Transport
import com.avito.android.util.DeviceSettingsChecker
import com.avito.logger.Logger
import com.avito.report.model.DeviceName
import com.avito.report.model.EntryTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient

interface InstrumentationDelegateProvider {

    fun get(context: Context): InstrumentationDelegate

    class Context(
        targetContext: android.content.Context,
        val environment: TestRunEnvironment.RunEnvironment,
        val tag: String
    ) {
        val friendlyErrorHandler = ReportFriendlyFailureHandler(targetContext)
        val deviceSettingsChecker = DeviceSettingsChecker(targetContext)
        val sentry = createSentry(environment.sentryDsn)

        val performanceTestReporter = PerformanceTestReporter()

        val testReportLogger: Logger = object : Logger {
            override fun debug(msg: String) {
                Log.d("TestReport", msg)
            }

            override fun exception(msg: String, error: Throwable) {
                Log.e("TestReport", msg, error)
            }

            override fun critical(msg: String, error: Throwable) {
                Log.e("TestReport", msg, error)
                sentry.sendException(error)
            }
        }

        val reportHttpClient: OkHttpClient by lazy { createReportHttpClient() }

        val testIssuesMonitor: TestIssuesMonitor = CompositeTestIssuesMonitor(
            sentry = sentry,
            testRunEnvironment = environment,
            logTag = tag
        )

        val report: Report

        init {
            val isLocalRun = environment.teamcityBuildId == TestRunEnvironment.LOCAL_STUDIO_RUN_ID
            val transport: List<Transport> = when {
                isLocalRun -> {
                    val reportConfig = environment.reportConfig
                    if (reportConfig != null) {
                        listOf(
                            LocalRunTransport(
                                reportApiHost = reportConfig.reportApiUrl,
                                reportFallbackUrl = reportConfig.reportApiFallbackUrl,
                                reportViewerUrl = reportConfig.reportViewerUrl,
                                reportCoordinates = environment.testRunCoordinates,
                                deviceName = DeviceName(environment.deviceName),
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

            report = ReportImplementation(
                sentry = sentry,
                fileStorageUrl = environment.fileStorageUrl,
                onDeviceCacheDirectory = environment.outputDirectory,
                httpClient = reportHttpClient,
                onIncident = { testIssuesMonitor.onFailure(it) },
                performanceTestReporter = performanceTestReporter,
                transport = transport,
                logger = testReportLogger
            )
        }
    }
}