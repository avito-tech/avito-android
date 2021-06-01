package com.avito.impact.plugin

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.plugin.internal.ImpactMetricsSender
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ImpactMetricsTask : DefaultTask() {

    @TaskAction
    fun doWork() {
        val statsd = project.statsd.get()
        val environmentInfo = project.environmentInfo().get()
        val finder = ModifiedProjectsFinder.from(project)

        val metricsTracker = BuildMetricTracker(environmentInfo, statsd)
        val sender = ImpactMetricsSender(finder, environmentInfo, metricsTracker)

        sender.sendMetrics()
    }
}
