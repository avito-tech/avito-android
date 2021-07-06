package com.avito.ci.steps

import com.avito.ci.internal.ReportKey
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.MarkReportAsSourceTask
import com.avito.test.summary.TestSummaryExtension
import com.avito.test.summary.TestSummaryFactory
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

public class MarkReportAsSourceForTMSStep(context: String, name: String) : TestSummaryPluginBuildStep(context, name) {

    override val stepName: String = "MarkReportAsSourceForTMSStep"

    override fun getOrCreateTask(
        rootTasksContainer: TaskContainer,
        extension: TestSummaryExtension,
        reportCoordinates: ReportCoordinates,
        testSummaryFactory: TestSummaryFactory
    ): TaskProvider<out Task> {
        val reportKey = ReportKey.fromReportCoordinates(reportCoordinates)

        val taskName = reportKey.appendToTaskName("markReportForTms")

        return rootTasksContainer.typedNamedOrNull(taskName)
            ?: rootTasksContainer.register<MarkReportAsSourceTask>(taskName) {
                group = "ci"
                description = "Marks this test report as source of truth for Avito TMS"

                this.reportCoordinates.set(reportCoordinates)
                this.reportsApi.set(testSummaryFactory.createReportsApi(extension))
                this.timeProvider.set(testSummaryFactory.timeProvider)
            }
    }
}
