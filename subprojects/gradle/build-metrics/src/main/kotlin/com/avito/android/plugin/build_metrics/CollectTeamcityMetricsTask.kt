package com.avito.android.plugin.build_metrics

import com.avito.android.stats.statsdConfig
import com.avito.teamcity.teamcityCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class CollectTeamcityMetricsTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @TaskAction
    fun action() {
        val buildId: String? = System.getenv("TMCT_METRICS_BUILD_ID")

        require(!buildId.isNullOrBlank()) { "TMCT_METRICS_BUILD_ID env must be set" }

        @Suppress("UnstableApiUsage")
        workerExecutor.noIsolation().submit(CollectTeamcityMetricsAction::class.java) { parameters ->
            parameters.getBuildId().set(buildId)
            parameters.getLogger().set(project.ciLogger)
            parameters.getStatsdConfig().set(project.statsdConfig)
            parameters.getTeamcityCredentials().set(project.teamcityCredentials)
        }
    }
}
