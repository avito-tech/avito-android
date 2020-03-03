package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.android.plugin.build_metrics.CollectTeamcityMetricsWorkerAction.Parameters
import com.avito.logger.Logger
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import com.avito.utils.logging.CILogger
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@Suppress("UnstableApiUsage")
abstract class CollectTeamcityMetricsWorkerAction : WorkAction<Parameters> {

    interface Parameters : WorkParameters {
        fun getBuildId(): Property<String>
        fun getTeamcityCredentials(): Property<TeamcityCredentials>
        fun getGraphiteConfig(): Property<GraphiteConfig>
        fun getLogger(): Property<CILogger>
    }

    override fun execute() {
        require(!parameters.getBuildId().orNull.isNullOrBlank()) { "teamcity buildId property must be set" }

        val graphite = graphiteSender()
        val teamcity = TeamcityApi.Impl(parameters.getTeamcityCredentials().get())
        val action = CollectTeamcityMetricsAction(
            buildId = parameters.getBuildId().get(),
            teamcityApi = teamcity,
            graphite = graphite
        )
        action.execute()
    }

    private fun graphiteSender(): GraphiteSender {
        val config = parameters.getGraphiteConfig().get()

        val ciLogger = parameters.getLogger().get()
        val logger = object : Logger {

            override fun debug(msg: String) {
                if (config.debug) {
                    ciLogger.debug(msg)
                }
            }

            override fun exception(msg: String, error: Throwable) = ciLogger.info(msg, error)

            override fun critical(msg: String, error: Throwable) = ciLogger.critical(msg, error)
        }
        return GraphiteSender.Impl(config, logger)
    }
}
