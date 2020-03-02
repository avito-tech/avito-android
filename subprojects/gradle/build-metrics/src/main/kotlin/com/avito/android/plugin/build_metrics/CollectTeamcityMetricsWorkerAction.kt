package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.android.plugin.build_metrics.CollectTeamcityMetricsWorkerAction.Parameters
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
        val logger = parameters.getLogger().get()
        val graphite: GraphiteSender = GraphiteSender.Impl(parameters.getGraphiteConfig().get()) { msg, error ->
            logger.info(msg, error)
        }
        val teamcity = TeamcityApi.Impl(parameters.getTeamcityCredentials().get())
        val action = CollectTeamcityMetricsAction(
            buildId = parameters.getBuildId().get(),
            teamcityApi = teamcity,
            graphite = graphite
        )
        action.execute()
    }
}
