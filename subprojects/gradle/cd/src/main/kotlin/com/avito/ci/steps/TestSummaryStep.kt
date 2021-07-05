package com.avito.ci.steps

import com.avito.ci.internal.ReportKey
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.report.model.Team
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.TestSummaryExtension
import com.avito.test.summary.TestSummaryFactory
import com.avito.test.summary.TestSummaryTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

public class TestSummaryStep(context: String, name: String) : TestSummaryPluginBuildStep(context, name) {

    override val stepName: String = "TestSummary"

    override fun getOrCreateTask(
        rootTasksContainer: TaskContainer,
        extension: TestSummaryExtension,
        reportCoordinates: ReportCoordinates,
        testSummaryFactory: TestSummaryFactory,
    ): TaskProvider<out Task> {

        val reportKey = ReportKey.fromReportCoordinates(reportCoordinates)

        val taskName = reportKey.appendToTaskName("testSummary")

        return rootTasksContainer.typedNamedOrNull<TestSummaryTask>(taskName)
            ?: rootTasksContainer.register<TestSummaryTask>(taskName) {
                group = "ci"
                description = "Sends test summary report to slack for [" +
                    "planSlug:${reportCoordinates.planSlug}, " +
                    "jobSlug:${reportCoordinates.jobSlug}" +
                    "]"

                this.reportCoordinates.set(reportCoordinates)
                this.summaryChannel.set(extension.summaryChannel)
                this.buildUrl.set(extension.buildUrl)
                this.unitToChannelMapping.set(
                    extension.unitToChannelMapping
                        .map { map -> map.map { (key, value) -> key to value }.toMap() }
                )
                this.mentionOnFailures.set(extension.mentionOnFailures.map { set -> set.map { Team(it) }.toSet() })
                this.reserveSlackChannel.set(extension.reserveSlackChannel)
                this.slackUsername.set(extension.slackUserName)
                this.slackClient.set(testSummaryFactory.createSlackClient(extension))
                this.reportsApi.set(testSummaryFactory.createReportsApi(extension))
                this.reportViewerUrl.set(extension.reportViewerUrl)
            }
    }
}
