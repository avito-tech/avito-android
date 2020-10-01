package com.avito.test.summary

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.statsdConfig
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.ciLogger
import com.avito.utils.logging.commonLogger
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class TestSummaryTask : DefaultTask() {

    @get:Input
    abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    abstract val slackToken: Property<String>

    @get:Input
    abstract val slackWorkspace: Property<String>

    @get:Input
    abstract val reportsHost: Property<String>

    @get:Input
    abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    abstract val buildUrl: Property<String>

    @get:Input
    abstract val reportViewerUrl: Property<String>

    @get:Input
    abstract val unitToChannelMapping: Property<Map<Team, SlackChannel>>

    @get:Input
    abstract val mentionOnFailures: Property<Set<Team>>

    @get:Input
    abstract val reserveSlackChannel: Property<SlackChannel>

    @get:Input
    abstract val slackUserName: Property<String>

    @TaskAction
    fun doWork() {
        val logger = commonLogger(ciLogger)

        val action: SendStatisticsAction = SendStatisticsActionImpl(
            reportApi = ReportsApi.create(
                host = reportsHost.get(),
                fallbackUrl = reportsHost.get(),
                logger = logger
            ),
            testSummarySender = TestSummarySenderImpl(
                slackClient = createSlackClient(
                    slackToken = slackToken.get(),
                    slackWorkspace = slackWorkspace.get()
                ),
                reportViewer = createReportViewer(reportViewerUrl.get()),
                logger = ciLogger,
                buildUrl = buildUrl.get(),
                reportCoordinates = reportCoordinates.get(),
                globalSummaryChannel = summaryChannel.get(),
                unitToChannelMapping = unitToChannelMapping.get(),
                mentionOnFailures = mentionOnFailures.get(),
                reserveSlackChannel = reserveSlackChannel.get(),
                slackUserName = slackUserName.get()
            ),
            graphiteRunWriter = GraphiteRunWriter(createStatsDSender(project.statsdConfig.get())),
            ciLogger = ciLogger
        )

        action.send(reportCoordinates.get())
    }

    private fun createSlackClient(slackToken: String, slackWorkspace: String): SlackClient {
        return SlackClient.Impl(
            token = slackToken,
            workspace = slackWorkspace
        )
    }

    private fun createReportViewer(reportViewerUrl: String): ReportViewer {
        return ReportViewer.Impl(reportViewerUrl)
    }

    private fun createStatsDSender(statsDConfig: StatsDConfig): StatsDSender {
        return StatsDSender.Impl(
            config = statsDConfig,
            logger = { message, error ->
                if (error != null) {
                    logger.info(message, error)
                } else {
                    logger.debug(message)
                }
            }
        )
    }
}
