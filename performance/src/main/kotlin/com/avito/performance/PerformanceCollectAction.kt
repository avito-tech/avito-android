package com.avito.performance

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.utils.logging.CILogger
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class PerformanceCollectAction(
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

    data class Params(
        val logger: CILogger,
        val graphiteKey: String,
        val reportCoordinates: ReportCoordinates,
        val buildId: String,
        val reportApiUrl: String,
        val reportApiFallbackUrl: String,
        val performanceTests: File,
        val pullRequestId: Int?,
        val buildUrl: String?,
        val bitbucketConfig: BitbucketConfig,
        val statsdConfig: StatsDConfig,
        val slackConfig: SlackConfig
    ) : Serializable {
        companion object
    }

    @Inject
    constructor(params: Params) : this(params, params.logger)

    override fun run() {
        try {
            val performanceTestFile = params.performanceTests

            val performanceResults = PerformanceTestCollector(
                reports = reports,
                id = params.reportCoordinates,
                buildId = params.buildId
            ).collect()

            PerformanceWriter().write(performanceResults, performanceTestFile)

            PerformanceTestStatsdSender(
                PerformanceMetricSender.Impl(
                    statsSender, params.graphiteKey,
                    buildUrl = params.buildUrl,
                    bitbucket = Bitbucket.create(
                        bitbucketConfig = params.bitbucketConfig,
                        logger = logger,
                        pullRequestId = params.pullRequestId
                    ),
                    slackConfig = params.slackConfig
                )
            ).sendSample(
                performanceResults
            )
        } catch (e: Throwable) {
            logger.critical("PerformanceCollectTask error", e)
        }
    }
}
