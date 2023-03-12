package com.avito.android.plugin.build_metrics.internal.teamcity

import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.teamcity.TeamcityApi
import org.jetbrains.teamcity.rest.Build

internal class CollectTeamcityMetricsAction(
    val buildId: String,
    private val teamcityApi: TeamcityApi,
    private val sender: BuildMetricSender,
) {

    fun execute() {
        val build = teamcityApi.getBuild(buildId)
        sendMetric(build)
    }

    private fun sendMetric(build: Build) {
        val metric = TeamcityBuildMetric(
            build = build,
        )

        try {
            sender.send(metric)
        } catch (e: Exception) {
            throw RuntimeException("Failed to send metric $metric", e)
        }
    }
}
