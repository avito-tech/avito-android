package com.avito.instrumentation.report

import com.avito.instrumentation.internal.report.InMemoryReport
import com.avito.instrumentation.internal.report.ReportImpl
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider
import org.funktionale.tries.Try
import java.io.Serializable

public interface Report : ReadReport {

    public interface Factory : Serializable {

        public sealed class Config : Serializable {

            public data class ReportViewerCoordinates(
                val reportCoordinates: ReportCoordinates,
                val buildId: String
            ) : Config()

            public data class ReportViewerId(
                val reportId: String
            ) : Config()

            public data class InMemory(
                val id: String
            ) : Config()
        }

        public fun createReport(config: Config): Report

        public fun createReadReport(config: Config): ReadReport

        public class StrategyFactory(
            private val factories: Map<String, Factory>
        ) : Factory, Serializable {

            override fun createReport(config: Config): Report =
                getFactory(config).createReport(config)

            override fun createReadReport(config: Config): ReadReport =
                getFactory(config).createReadReport(config)

            private fun getFactory(config: Config): Factory =
                requireNotNull(factories[config::class.java.simpleName]) {
                    "Factory for config: $config hasn't found. You must register"
                }
        }

        public class InMemoryReportFactory(private val timeProvider: TimeProvider) : Factory {

            @Transient
            private var reports: MutableMap<Config.InMemory, InMemoryReport> = mutableMapOf()

            // TODO problems with serialization
            @Synchronized
            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, {
                        InMemoryReport(
                            id = config.id,
                            timeProvider = timeProvider
                        )
                    })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }

            @Synchronized
            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, {
                        InMemoryReport(
                            id = config.id,
                            timeProvider = timeProvider
                        )
                    })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }
        }

        public class ReportViewerFactory(
            public val reportApiUrl: String,
            public val loggerFactory: LoggerFactory,
            public val timeProvider: TimeProvider,
            public val verboseHttp: Boolean
        ) : Factory {

            @Transient
            private lateinit var reportsApi: ReportsApi

            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        ReportImpl(
                            reportsApi = reportsApi,
                            loggerFactory = loggerFactory,
                            reportCoordinates = config.reportCoordinates,
                            buildId = config.buildId,
                            timeProvider = timeProvider
                        )
                    }
                    else -> throwUnsupportedConfigException(config)
                }
            }

            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        ReadReport.ReportCoordinates(
                            reportsFetchApi = reportsApi,
                            coordinates = config.reportCoordinates
                        )
                    }
                    is Config.ReportViewerId -> {
                        ensureInitializedReportsApi()
                        ReadReport.Id(
                            reportsFetchApi = reportsApi,
                            id = config.reportId
                        )
                    }
                    is Config.InMemory -> TODO("Unsupported type")
                }
            }

            private fun throwUnsupportedConfigException(config: Config): Nothing {
                throw IllegalArgumentException("Unsupported config: $config")
            }

            private fun ensureInitializedReportsApi() {
                if (!::reportsApi.isInitialized) {
                    reportsApi = ReportsApi.create(
                        host = reportApiUrl,
                        loggerFactory = loggerFactory,
                        verboseHttp = verboseHttp
                    )
                }
            }
        }
    }

    public fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String)

    public fun tryGetId(): String?

    public fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    public fun sendCompletedTest(completedTest: AndroidTest.Completed)

    public fun finish()

    public fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit>

    public fun getCrossDeviceTestData(): Try<CrossDeviceSuite>

    public companion object
}
