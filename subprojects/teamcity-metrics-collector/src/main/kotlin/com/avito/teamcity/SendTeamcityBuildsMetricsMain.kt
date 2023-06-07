package com.avito.teamcity

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import com.avito.teamcity.builds.TeamcityBuildsProvider
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required

@ExperimentalCli
internal object SendTeamcityBuildsMetricsMain {

    class SendMetrics : Subcommand(
        "sendMetrics",
        "Send build duration and build queue metrics for specific build type [metricsSourceBuildType]"
    ) {

        /**
         * This build type is metrics source
         */
        private val metricsSourceBuildType: String by option(type = ArgType.String).required()

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

        private val graphiteSender by lazy {
            GraphiteSender.create(
                config = GraphiteConfig(
                    isEnabled = true,
                    enableDetailedLogs = true,
                    host = graphiteHost,
                    port = graphitePort,
                    metricPrefix = SeriesName.create(metricsPrefix, true)
                ),
                loggerFactory = PrintlnLoggerFactory,
                isTest = false
            )
        }

        private val teamcityBuildsProvider by lazy {
            TeamcityBuildsProvider.create(
                api = TeamcityApi.create(
                    TeamcityCredentials(
                        url = teamcityUrl,
                        user = teamcityApiUser,
                        password = teamcityApiPassword
                    )
                ),
                loggerFactory = PrintlnLoggerFactory,
            )
        }

        override fun execute() {
            val action = SendTeamcityBuildMetricsAction(
                graphiteSender = graphiteSender,
                teamcityBuildsProvider = teamcityBuildsProvider
            )
            action.execute(metricsSourceBuildType)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("teamcity-metrics-sender")
        parser.subcommands(SendMetrics())
        parser.parse(args)
    }
}
