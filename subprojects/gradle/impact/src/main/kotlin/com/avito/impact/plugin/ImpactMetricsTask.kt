package com.avito.impact.plugin

import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.plugin.internal.ImpactMetricsSender
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public abstract class ImpactMetricsTask : DefaultTask() {

    @TaskAction
    public fun doWork() {
        val environmentInfo = project.environmentInfo().get()
        val finder = ModifiedProjectsFinder.from(project)

        val metricsTracker = project.statsd.get()
        val sender = ImpactMetricsSender(finder, environmentInfo, metricsTracker)

        sender.sendMetrics()
    }
}
