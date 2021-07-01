package com.avito.test.summary

import com.avito.logger.GradleLoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.time.TimeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class MarkReportAsSourceTask : DefaultTask() {

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Internal
    public abstract val reportsApi: Property<ReportsApi>

    @get:Internal
    public abstract val timeProvider: Property<TimeProvider>

    @TaskAction
    public fun doWork() {
        MarkReportAsSourceAction(
            reportsApi = reportsApi.get(),
            timeProvider = timeProvider.get(),
            loggerFactory = GradleLoggerFactory.fromTask(this)
        ).mark(reportCoordinates = reportCoordinates.get())
    }
}
