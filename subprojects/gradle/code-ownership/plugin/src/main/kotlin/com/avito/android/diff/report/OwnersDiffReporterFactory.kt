package com.avito.android.diff.report

import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.report.file.FileOwnersDiffReporter
import com.avito.android.diff.report.slack.SlackClientFactory
import com.avito.android.diff.report.slack.SlackOwnersDiffReporter

public class OwnersDiffReporterFactory(private val messageFormatter: OwnersDiffMessageFormatter) {

    public fun create(destination: OwnersDiffReportDestination): OwnersDiffReporter {
        return when (destination) {
            is OwnersDiffReportDestination.Custom -> destination.reporter
            is OwnersDiffReportDestination.File -> FileOwnersDiffReporter(destination.parentDir, messageFormatter)
            is OwnersDiffReportDestination.Slack -> SlackOwnersDiffReporter(
                notificationClient = SlackClientFactory.create(destination.token, destination.workspace),
                slackChannel = destination.channel,
                slackUserName = destination.userName,

                messageFormatter = messageFormatter
            )
        }
    }
}
