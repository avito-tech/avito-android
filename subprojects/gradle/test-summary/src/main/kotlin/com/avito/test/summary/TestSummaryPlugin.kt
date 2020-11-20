package com.avito.test.summary

import com.avito.logger.Logger
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.logging.ciLogger
import com.avito.utils.logging.commonLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<TestSummaryExtension>(testSummaryExtensionName)

        val logger = commonLogger(target.ciLogger)

        @Suppress("UnstableApiUsage")
        val slackClient: Provider<SlackClient> =
            extension.slackToken.zip(extension.slackWorkspace) { token, workspace ->
                createSlackClient(token, workspace)
            }

        val reportsApi: Provider<ReportsApi> = extension.reportsHost.map { createReportsApi(it, logger) }

        val timeProvider: TimeProvider = DefaultTimeProvider()

        val reportViewer: Provider<ReportViewer> = extension.reportViewerUrl.map { createReportViewer(it) }

        // report coordinates provided in TestSummaryStep
        // this plugin only works via steps for now
        target.tasks.register<TestSummaryTask>(testSummaryTaskName) {
            summaryChannel.set(extension.summaryChannel)
            buildUrl.set(extension.buildUrl)

            @Suppress("UnstableApiUsage")
            unitToChannelMapping.set(extension.unitToChannelMapping
                .map { map -> map.map { (key, value) -> Team(key) to SlackChannel(value) }.toMap() }
            )

            mentionOnFailures.set(extension.mentionOnFailures.map { set -> set.map { Team(it) }.toSet() })
            reserveSlackChannel.set(extension.reserveSlackChannel)
            slackUserName.set(extension.slackUserName)

            this.slackClient.set(slackClient)
            this.reportsApi.set(reportsApi)
            this.reportViewer.set(reportViewer)
        }

        target.tasks.register<FlakyReportTask>(flakyReportTaskName) {
            summaryChannel.set(extension.summaryChannel)
            slackUsername.set(extension.slackUserName)
            buildUrl.set(extension.buildUrl)
            currentBranch.set(extension.currentBranch)

            this.slackClient.set(slackClient)
            this.timeProvider.set(timeProvider)
            this.reportsApi.set(reportsApi)
            this.reportViewer.set(reportViewer)
        }
    }

    private fun createReportViewer(reportViewerUrl: String): ReportViewer {
        return ReportViewer.Impl(reportViewerUrl)
    }

    private fun createSlackClient(slackToken: String, slackWorkspace: String): SlackClient {
        return SlackClient.Impl(
            token = slackToken,
            workspace = slackWorkspace
        )
    }

    private fun createReportsApi(reportsHost: String, logger: Logger): ReportsApi {
        return ReportsApi.create(
            host = reportsHost,
            fallbackUrl = reportsHost,
            logger = logger
        )
    }
}
