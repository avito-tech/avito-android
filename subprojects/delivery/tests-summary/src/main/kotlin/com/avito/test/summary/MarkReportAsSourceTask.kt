package com.avito.test.summary

import com.avito.android.stats.statsdConfig
import com.avito.logger.GradleLoggerPlugin
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public abstract class MarkReportAsSourceTask : DefaultTask() {

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    public abstract val reportsHost: Property<String>

    @TaskAction
    public fun doWork() {
        val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(this).get()
        val di = TestSummaryDI(
            project.statsdConfig,
            loggerFactory
        )
        val reportsApi = di.provideReportsApi(reportsHost.get())

        MarkReportAsSourceAction(
            reportsApi = reportsApi,
            timeProvider = di.timeProvider,
            loggerFactory = loggerFactory
        ).mark(reportCoordinates = reportCoordinates.get())
    }
}
