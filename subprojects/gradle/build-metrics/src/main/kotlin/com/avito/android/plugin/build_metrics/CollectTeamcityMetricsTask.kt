package com.avito.android.plugin.build_metrics

import com.avito.android.stats.statsdConfig
import com.avito.teamcity.teamcityCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class CollectTeamcityMetricsTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
    objects: ObjectFactory
) : DefaultTask() {

    @Input
    val buildId = objects.property<String>()

    @TaskAction
    fun action() {
        require(!buildId.orNull.isNullOrBlank()) { "teamcity buildId property must be set" }

        @Suppress("UnstableApiUsage")
        workerExecutor.noIsolation().submit(CollectTeamcityMetricsWorkerAction::class.java) { parameters ->
            parameters.getBuildId().set(buildId)
            parameters.getLogger().set(project.ciLogger)
            parameters.getStatsdConfig().set(project.statsdConfig)
            parameters.getTeamcityCredentials().set(project.teamcityCredentials)
        }
    }
}
