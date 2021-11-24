package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.plugin.build_metrics.internal.teamcity.CollectTeamcityMetricsWorkerAction
import com.avito.logger.LoggerFactory
import com.avito.teamcity.TeamcityCredentials
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

public abstract class CollectTeamcityMetricsTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Input
    public abstract val buildId: Property<String>

    @get:Input
    public abstract val graphiteConfig: Property<GraphiteConfig>

    @get:Input
    public abstract val teamcityCredentials: Property<TeamcityCredentials>

    @get:Internal
    public abstract val loggerFactory: Property<LoggerFactory>

    @TaskAction
    public fun action() {
        workerExecutor.noIsolation().submit(CollectTeamcityMetricsWorkerAction::class.java) { parameters ->
            parameters.getBuildId().set(buildId)
            parameters.getLoggerFactory().set(loggerFactory)
            parameters.getGraphiteConfig().set(graphiteConfig)
            parameters.getTeamcityCredentials().set(teamcityCredentials)
        }
    }
}
