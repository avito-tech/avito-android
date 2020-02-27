package com.avito.android.plugin.build_metrics

import com.avito.android.plugin.build_metrics.CollectTeamcityMetricsAction.Parameters
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.android.stats.graphiteSeries
import com.avito.android.stats.graphiteSeriesElement
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import com.avito.utils.logging.CILogger
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

@Suppress("UnstableApiUsage")
abstract class CollectTeamcityMetricsAction : WorkAction<Parameters> {

    interface Parameters : WorkParameters {
        fun getBuildId(): Property<String>
        fun getTeamcityCredentials(): Property<TeamcityCredentials>
        fun getStatsdConfig(): Property<StatsDConfig>
        fun getLogger(): Property<CILogger>
    }

    @VisibleForTesting
    open val teamcity: TeamcityApi
        get() = TeamcityApi.Impl(parameters.getTeamcityCredentials().get())

    @VisibleForTesting
    open val statsd: StatsDSender
        get() = StatsDSender.Impl(parameters.getStatsdConfig().get()) { msg, _ ->
            parameters.getLogger().get().info(msg)
        }

    override fun execute() {
        val build = teamcity.getBuild(parameters.getBuildId().get())
        sendMetric(build)
    }

    private fun sendMetric(build: Build) {
        val buildId = graphiteSeriesElement(build.id.stringId)
        val buildTypeId = graphiteSeries(build.buildConfigurationId.stringId)
        val buildStatus = graphiteSeriesElement(build.status?.name ?: "unknown")
        val duration = Duration.between(build.startDateTime, build.finishDateTime)

        // we use a redundant structure only for compatibility reasons
        val path = "ci.builds.teamcity.duration" +
            ".build_type_id.${buildTypeId}" +
            ".id.${buildId}" +
            ".agent._" +
            ".state._" +
            ".status.${buildStatus}" +
            "._._._._"
        statsd.send(metric = TimeMetric(path, duration.toMillis()))
    }
}
