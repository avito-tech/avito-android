package com.avito.teamcity

import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSenderImpl
import com.avito.android.clickstream.config.ClickStreamConfig
import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import com.avito.teamcity.builds.PreviousMetricsSendingTimeProvider
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.config.TeamcityMetricsSourceConfig
import com.avito.teamcity.saturate.bitbucket.BitbucketInfoSaturator
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@ExperimentalCli
internal object SendTeamcityBuildsMetricsMain {

    class SendMetrics : Subcommand(
        "sendMetrics",
        "Send build duration and build queue metrics for specific build type [metricsSourceBuildType]"
    ) {

        /**
         * Absolute path to [TeamcityMetricsSourceConfig]
         */
        private val metricsSourcesConfigPath: String by option(type = ArgType.String)
            .required()

        private val teamcityUrl: String by option(type = ArgType.String)
            .required()

        private val teamcityApiUser: String by option(type = ArgType.String)
            .required()

        private val teamcityApiPassword: String by option(type = ArgType.String)
            .required()

        private val graphiteHost: String by option(type = ArgType.String)
            .required()

        private val graphitePort: Int by option(type = ArgType.Int)
            .required()

        private val metricsPrefix: String by option(type = ArgType.String)
            .required()

        private val clickstreamServiceUrl: String by option(type = ArgType.String)
            .required()

        private val bitbucketUrl: String by option(type = ArgType.String)
            .required()

        private val bitbucketUser: String by option(type = ArgType.String)
            .required()

        private val bitbucketPassword: String by option(type = ArgType.String)
            .required()

        private val graphiteSender by lazy {
            GraphiteSender.create(
                config = GraphiteConfig(
                    isEnabled = true,
                    enableDetailedLogs = true,
                    host = graphiteHost,
                    port = graphitePort,
                    metricPrefix = SeriesName.create(metricsPrefix, true),
                    ignoreExceptions = false,
                ),
                loggerFactory = PrintlnLoggerFactory,
                isTest = false,
            )
        }

        private val teamcityApi by lazy {
            TeamcityApi.create(
                TeamcityCredentials(
                    url = teamcityUrl,
                    user = teamcityApiUser,
                    password = teamcityApiPassword,
                )
            )
        }

        private val teamcityBuildsProvider by lazy {
            TeamcityBuildsProvider.create(
                api = teamcityApi,
                loggerFactory = PrintlnLoggerFactory,
            )
        }

        private val previousMetricsSendingTimeProvider by lazy {
            PreviousMetricsSendingTimeProvider.create(teamcityApi)
        }

        private val clickstreamTracker by lazy {
            ClickStreamEventTracker(
                clickStreamSender = ClickStreamSenderImpl(
                    config = ClickStreamConfig(
                        serviceUrl = clickstreamServiceUrl,
                        readTimeOutInSeconds = 10,
                        connectTimeOutInSeconds = 10,
                    )
                )
            )
        }

        private val bitbucketInfoSaturator by lazy {
            BitbucketInfoSaturator(
                bitbucketHost = bitbucketUrl,
                bitbucketUser = bitbucketUser,
                bitbucketPassword = bitbucketPassword,
            )
        }

        override fun execute() {
            val action = SendTeamcityBuildMetricsAction(
                graphiteSender = graphiteSender,
                teamcityBuildsProvider = teamcityBuildsProvider,
                previousMetricsSendingTimeProvider = previousMetricsSendingTimeProvider,
                clickstreamTracker = clickstreamTracker,
                bitbucketInfoSaturator = bitbucketInfoSaturator
            )
            val config = Json.decodeFromString<TeamcityMetricsSourceConfig>(
                string = File(metricsSourcesConfigPath).readText(),
            )
            action.execute(config.sources)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("teamcity-metrics-sender")
        parser.subcommands(SendMetrics())
        parser.parse(args)
    }
}
