package com.avito.performance

import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.AtlassianCredentials
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.logger.Logger
import com.avito.performance.stats.Stats
import com.avito.performance.stats.StatsApi
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.ReportsApi
import com.avito.report.model.PerformanceTest
import com.avito.utils.getStackTraceString
import com.avito.utils.logging.CILogger
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File
import java.io.Serializable
import javax.inject.Inject

open class PerformanceCompareAction(
    private val params: Params,
    private val logger: CILogger,
    private val reports: ReportsApi = ReportsApi.create(
        host = params.reportApiUrl,
        fallbackUrl = params.reportApiFallbackUrl,
        logger = object : Logger {
            override fun debug(msg: String) {
                logger.debug(msg)
            }

            override fun exception(msg: String, error: Throwable) {
                logger.debug(msg, error)
            }

            override fun critical(msg: String, error: Throwable) {
                logger.debug(msg, error)
            }
        },
        verboseHttp = false
    ),
    private val statsSender: StatsDSender = StatsDSender.Impl(
        config = params.statsdConfig,
        logger = { message, error -> if (error != null) logger.info(message, error) else logger.debug(message) })
) : Runnable {

    private val stats: Stats = Stats.Impl(
        api = StatsApi.Impl(
            logger = logger,
            verbose = false,
            url = params.statsUrl
        )
    )

    data class Params(
        val logger: CILogger,
        val reportApiUrl: String,
        val reportApiFallbackUrl: String,
        val enablePrPerformanceReporting: Boolean,
        val previousTests: File?,
        val currentTests: File,
        val comparison: File,
        val pullRequestId: Int?,
        val atlassianCredentials: AtlassianCredentials,
        val buildUrl: String?,
        val bitbucketConfig: BitbucketConfig,
        val statsdConfig: StatsDConfig,
        val slackConfig: SlackConfig,
        val statsUrl: String
    ) : Serializable {
        companion object
    }

    //used in workers api submit
    @Suppress("unused")
    @Inject
    constructor(params: Params) : this(params, params.logger)

    override fun run() {
        val metricPrefix = "ci.performance.${params.pullRequestId ?: "null"}.compare.outcome"

        try {
            val previousTestsFile = params.previousTests
            val currentTestsFile = params.currentTests

            if (previousTestsFile != null) {

                val runnedTests = Gson().fromJson<List<PerformanceTest>>(currentTestsFile.readText())
                val previousTests = Gson().fromJson<List<PerformanceTest>>(previousTestsFile.readText())

                val comparisonList = PerformanceTestComparator(stats).compare(runnedTests, previousTests)

                PerformanceWriter().write(comparisonList, params.comparison)

                report(comparisonList)
                statsSender.send(metricPrefix, CountMetric("success"))
            } else {
                statsSender.send(metricPrefix, CountMetric("missing"))
                logger.info("Previous performance test does not exist!")
            }
        } catch (e: Throwable) {
            statsSender.send(metricPrefix, CountMetric("failure"))
            logger.critical("PerformanceCompareTask error", e)
            with(
                SlackSender.Impl(
                    buildUrl = params.buildUrl,
                    slackConfig = params.slackConfig
                )
            ) {
                sendToSlack(
                    listOf(
                        "PerformanceCompareTask error",
                        e.getStackTraceString()
                    )
                    , ATTACHMENT_COLOR_RED
                )
            }
        }
    }

    private fun report(comparisonList: List<ComparedTest.Comparison>) {
        PerformanceTestReporter(reports).reportSuccess(comparisonList)

        with(
            PerformanceTestStatsdSender(
                PerformanceMetricSender.Impl(
                    statsSender,
                    bitbucket = Bitbucket.create(
                        bitbucketConfig = params.bitbucketConfig,
                        logger = logger,
                        pullRequestId = params.pullRequestId
                    ),
                    slackSender = SlackSender.Impl(
                        buildUrl = params.buildUrl,
                        slackConfig = params.slackConfig
                    )
                )
            )
        ) {
            sendCompare(comparisonList)
            if (params.enablePrPerformanceReporting) {
                reportToPr(comparisonList)
            }
        }
    }
}
