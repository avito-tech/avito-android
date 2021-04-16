package com.avito.test.summary

import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.ReportsApiFactory
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<TestSummaryExtension>(testSummaryExtensionName)

        @Suppress("UnstableApiUsage")
        val slackClient: Provider<SlackClient> =
            extension.slackToken.zip(extension.slackWorkspace) { token, workspace ->
                createSlackClient(token, workspace)
            }

        val loggerFactory = GradleLoggerFactory.fromPlugin(this, target)

        val timeProvider: TimeProvider = DefaultTimeProvider()

        val reportsApi: Provider<ReportsApi> = extension.reportsHost.map {
            createReportsApi(
                reportsHost = it,
                loggerFactory = loggerFactory,
                httpClientProvider = HttpClientProvider(
                    statsDSender = target.statsd.get(),
                    timeProvider = timeProvider,
                    loggerFactory = loggerFactory
                )
            )
        }

        // report coordinates provided in TestSummaryStep
        // this plugin only works via steps for now
        target.tasks.register<TestSummaryTask>(testSummaryTaskName) {
            summaryChannel.set(extension.summaryChannel)
            buildUrl.set(extension.buildUrl)

            @Suppress("UnstableApiUsage")
            unitToChannelMapping.set(
                extension.unitToChannelMapping
                    .map { map -> map.map { (key, value) -> key to value }.toMap() }
            )

            mentionOnFailures.set(extension.mentionOnFailures.map { set -> set.map { Team(it) }.toSet() })
            reserveSlackChannel.set(extension.reserveSlackChannel)
            slackUserName.set(extension.slackUserName)

            this.slackClient.set(slackClient)
            this.reportsApi.set(reportsApi)
            this.reportViewerUrl.set(reportViewerUrl)
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

    private fun createSlackClient(slackToken: String, slackWorkspace: String): SlackClient {
        return SlackClient.Impl(
            token = slackToken,
            workspace = slackWorkspace
        )
    }

    private fun createReportsApi(
        reportsHost: String,
        loggerFactory: LoggerFactory,
        httpClientProvider: HttpClientProvider
    ): ReportsApi {
        return ReportsApiFactory.create(
            host = reportsHost,
            loggerFactory = loggerFactory,
            httpClientProvider = httpClientProvider
        )
    }
}
