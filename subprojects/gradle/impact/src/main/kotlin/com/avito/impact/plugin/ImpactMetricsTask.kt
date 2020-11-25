package com.avito.impact.plugin

import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.impact.ModifiedProjectsFinder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ImpactMetricsTask : DefaultTask() {

    @TaskAction
    fun doWork() {
        val statsd = project.statsd.get()
        val environmentInfo = project.environmentInfo().get()

        val sender = ImpactMetricsSender(statsd, environmentInfo)

        sender.sendModifiedProjectCounters(modifiedProjectsFinder = ModifiedProjectsFinder.from(project))
    }
}
