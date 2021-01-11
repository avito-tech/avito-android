package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.android.plugin.build_metrics.CollectTeamcityMetricsWorkerAction.Parameters
import com.avito.logger.LoggerFactory
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@Suppress("UnstableApiUsage")
abstract class CollectTeamcityMetricsWorkerAction : WorkAction<Parameters> {

    interface Parameters : WorkParameters {
        fun getBuildId(): Property<String>
        fun getTeamcityCredentials(): Property<TeamcityCredentials>
        fun getGraphiteConfig(): Property<GraphiteConfig>
        fun getLoggerFactory(): Property<LoggerFactory>
    }

    override fun execute() {
        val buildId: String? = parameters.getBuildId().orNull
        require(!buildId.isNullOrBlank()) { "teamcity buildId property must be set" }

        val graphite = graphiteSender()
        val teamcity = TeamcityApi.Impl(parameters.getTeamcityCredentials().get())
        val action = CollectTeamcityMetricsAction(
            buildId = buildId,
            teamcityApi = teamcity,
            graphite = graphite
        )
        action.execute()
    }

    private fun graphiteSender(): GraphiteSender {
        return GraphiteSender.Impl(
            config = parameters.getGraphiteConfig().get(),
            loggerFactory = parameters.getLoggerFactory().get()
        )
    }
}
