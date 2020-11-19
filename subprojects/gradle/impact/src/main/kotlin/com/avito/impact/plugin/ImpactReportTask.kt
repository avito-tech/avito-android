package com.avito.impact.plugin

import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.configuration.internalModule
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ImpactReportTask : DefaultTask() {

    @TaskAction
    fun doWork() {
        val modifiedProjectsFinder = ModifiedProjectsFinder.from(project)

        val statsd = project.statsd.get()
        val environmentInfo = project.environmentInfo().get()

        val impactMetricsSender = ImpactMetricsSender(statsd, environmentInfo)

        modifiedProjectsFinder.getProjects().forEach { (type, projects) ->
            val (modified, unmodified) = projects.partition { it.project.internalModule.isModified(type) }

            logger.lifecycle("For type: $type; Modules: ${projects.size}; modified: ${modified.size}; unmodified: ${unmodified.size}")

            impactMetricsSender.send(type, "all", projects.size)
            impactMetricsSender.send(type, "modified", modified.size)
            impactMetricsSender.send(type, "unmodified", unmodified.size)
        }
    }
}
