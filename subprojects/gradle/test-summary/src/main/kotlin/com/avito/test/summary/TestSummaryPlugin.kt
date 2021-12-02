package com.avito.test.summary

import com.avito.android.Problem
import com.avito.android.asPlainText
import com.avito.kotlin.dsl.isRoot
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<TestSummaryExtension>(testSummaryExtensionName)

        if (!target.isRoot()) {
            val problem = Problem(
                shortDescription = "TestSummaryPlugin should be applied to root project",
                context = "TestSummaryPlugin applied to ${target.path}",
                because = "Summary tasks now registered from CiStep on root project, one per PlanSlug+JobSlug key, " +
                    "to make cross-app dependency on single report possible"
            )
            target.logger.warn(problem.asPlainText())
        } else {

            val timeProvider: TimeProvider = DefaultTimeProvider()

            // report coordinates provided in FlakyReportStep
            // this plugin only works via steps for now
            target.tasks.register<FlakyReportTask>(flakyReportTaskName) {
                summaryChannel.set(extension.summaryChannel)
                slackUsername.set(extension.slackUserName)
                buildUrl.set(extension.buildUrl)
                currentBranch.set(extension.currentBranch)

                this.slackClient.set(slackClient)
                this.timeProvider.set(timeProvider)
                this.reportsApi.set(reportsApi)
                this.reportViewerUrl.set(extension.reportViewerUrl)
            }
        }
    }
}
