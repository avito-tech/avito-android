package com.avito.android.diff.report.slack

import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.model.OwnersDiff
import com.avito.android.diff.report.OwnersDiffReporter
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackSendMessageRequest

internal class SlackOwnersDiffReporter(
    private val slackClient: SlackClient,
    private val slackChannel: SlackChannel,
    private val slackUserName: String,
    private val messageFormatter: OwnersDiffMessageFormatter
) : OwnersDiffReporter {

    override fun reportDiffFound(diffs: OwnersDiff) {
        if (diffs.isEmpty()) return // no report on empty diffs
        val messageText = messageFormatter.formatDiffMessage(diffs)
        val slackMessage = SlackSendMessageRequest(slackChannel, messageText, slackUserName)
        slackClient.sendMessage(slackMessage)
    }
}
