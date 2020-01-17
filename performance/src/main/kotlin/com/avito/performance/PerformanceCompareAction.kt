package com.avito.performance

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.AtlassianCredentials
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.performance.stats.Stats
import com.avito.performance.stats.StatsApi
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.ReportsApi
import com.avito.report.model.PerformanceTest
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
        logger = { message, error -> logger.debug(message, error) }
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
        val previousTests: File,
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

    @Inject
    constructor(params: Params) : this(params, params.logger)

    override fun run() {
        try {
            val previousTestsFile = params.previousTests
            val currentTestsFile = params.currentTests

            if (previousTestsFile.exists() && previousTestsFile.length() > 0) {

                val runnedTests = Gson().fromJson<List<PerformanceTest>>(currentTestsFile.readText())
                val previousTests = Gson().fromJson<List<PerformanceTest>>(previousTestsFile.readText())

                val comparisonList = PerformanceTestComparator(stats).compare(runnedTests, previousTests)

                PerformanceWriter().write(comparisonList, params.comparison)

                report1(comparisonList)

            } else {
                logger.info("Previous performance test does not exist!")
            }
        } catch (e: Throwable) {
            logger.critical("PerformanceCompareTask error", e)
        }
    }

    private fun report1(comparisonList: List<ComparedTest.Comparison>) {
        PerformanceTestReporter(reports).reportSuccess(comparisonList)

        with(
            PerformanceTestStatsdSender(
                PerformanceMetricSender.Impl(
                    statsSender,
                    buildUrl = params.buildUrl,
                    bitbucket = Bitbucket.create(
                        bitbucketConfig = params.bitbucketConfig,
                        logger = logger,
                        pullRequestId = params.pullRequestId
                    ),
                    slackConfig = params.slackConfig
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
