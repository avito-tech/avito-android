package com.avito.plugin

import com.avito.logger.GradleLoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.time.DefaultTimeProvider
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

        MarkReportAsSourceAction(
            reportsApi = ReportsApi.create(
                host = reportsHost.get(),
                fallbackUrl = reportsHost.get(),
                loggerFactory = loggerFactory
            ),
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory
        ).mark(reportCoordinates = reportCoordinates.get())
    }
}
