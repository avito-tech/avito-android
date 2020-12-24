package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.graphiteConfig
import com.avito.logger.GradleLoggerFactory
import com.avito.teamcity.teamcityCredentials
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
        @Suppress("UnstableApiUsage")
        workerExecutor.noIsolation().submit(CollectTeamcityMetricsWorkerAction::class.java) { parameters ->
            parameters.getBuildId().set(buildId)
            parameters.getLoggerFactory().set(GradleLoggerFactory.fromTask(this))
            parameters.getGraphiteConfig().set(project.graphiteConfig)
            parameters.getTeamcityCredentials().set(project.teamcityCredentials)
        }
    }
}
