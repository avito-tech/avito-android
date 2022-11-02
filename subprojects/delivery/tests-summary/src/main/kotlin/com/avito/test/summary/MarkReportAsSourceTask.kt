package com.avito.test.summary

import Slf4jGradleLoggerFactory
import com.avito.reportviewer.ReportsApiFactory
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class MarkReportAsSourceTask : DefaultTask() {

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    public abstract val reportsHost: Property<String>

    @get:Internal
    public abstract val timeProvider: Property<TimeProvider>

    @TaskAction
    public fun doWork() {
        val reportsApi = ReportsApiFactory.create(
            host = reportsHost.get(),
            builder = OkHttpClient.Builder()
        )

        MarkReportAsSourceAction(
            reportsApi = reportsApi,
            timeProvider = timeProvider.get(),
            loggerFactory = Slf4jGradleLoggerFactory
        ).mark(reportCoordinates = reportCoordinates.get())
    }
}
