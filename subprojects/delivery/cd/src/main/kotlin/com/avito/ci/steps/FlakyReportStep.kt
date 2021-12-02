package com.avito.ci.steps

import com.avito.ci.internal.ReportKey
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.FlakyReportTask
import com.avito.test.summary.TestSummaryExtension
import com.avito.test.summary.TestSummaryFactory
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

public class FlakyReportStep(context: String, name: String) : TestSummaryPluginBuildStep(context, name) {

    override val stepName: String = "FlakyReport"

    override fun getOrCreateTask(
        rootTasksContainer: TaskContainer,
        extension: TestSummaryExtension,
        reportCoordinates: ReportCoordinates,
        testSummaryFactory: TestSummaryFactory,
    ): TaskProvider<out Task> {

        val reportKey = ReportKey.fromReportCoordinates(reportCoordinates)

        val taskName = reportKey.appendToTaskName("flakyReport")

        return rootTasksContainer.typedNamedOrNull<FlakyReportTask>(taskName)
            ?: rootTasksContainer.register<FlakyReportTask>(taskName) {
                group = "ci"
                description = "Sends flaky tests report to slack for [" +
                    "planSlug:${reportCoordinates.planSlug}, " +
                    "jobSlug:${reportCoordinates.jobSlug}" +
                    "]"

                this.reportCoordinates.set(reportCoordinates)
                this.summaryChannel.set(extension.summaryChannel)
                this.slackUsername.set(extension.slackUserName)
                this.buildUrl.set(extension.buildUrl)
                this.currentBranch.set(extension.currentBranch)
                this.timeProvider.set(testSummaryFactory.timeProvider)
                this.slackClient.set(testSummaryFactory.createSlackClient(extension))
                this.reportsApi.set(testSummaryFactory.createReportsApi(extension))
                this.reportViewerUrl.set(extension.reportViewerUrl)
            }
    }
}
