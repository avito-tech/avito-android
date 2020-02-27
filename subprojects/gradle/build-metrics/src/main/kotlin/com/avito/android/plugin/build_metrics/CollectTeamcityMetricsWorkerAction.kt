package com.avito.android.plugin.build_metrics

import com.avito.android.plugin.build_metrics.CollectTeamcityMetricsWorkerAction.Parameters
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
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
        fun getStatsdConfig(): Property<StatsDConfig>
        fun getLogger(): Property<CILogger>
    }

    override fun execute() {
        val logger = parameters.getLogger().get()
        val statsd: StatsDSender = StatsDSender.Impl(parameters.getStatsdConfig().get()) { msg, _ ->
            logger.info(msg)
        }
        val teamcity = TeamcityApi.Impl(parameters.getTeamcityCredentials().get())
        val action = CollectTeamcityMetricsAction(
            buildId = parameters.getBuildId().get(),
            teamcityApi = teamcity,
            statsd = statsd
        )
        action.execute()
    }
}
