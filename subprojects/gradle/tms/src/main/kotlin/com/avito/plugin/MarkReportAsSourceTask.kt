package com.avito.plugin

import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.logger.GradleLoggerFactory
import com.avito.report.ReportsApiFactory
import com.avito.report.model.ReportCoordinates
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class MarkReportAsSourceTask : DefaultTask() {

    @get:Input
    abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Internal
    abstract val reportsHost: Property<String>

    @TaskAction
    fun doWork() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val timeProvider: TimeProvider = DefaultTimeProvider()

        val httpClientProvider = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )

        MarkReportAsSourceAction(
            reportsApi = ReportsApiFactory.create(
                host = reportsHost.get(),
                loggerFactory = loggerFactory,
                httpClientProvider = httpClientProvider
            ),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        ).mark(reportCoordinates = reportCoordinates.get())
    }
}
