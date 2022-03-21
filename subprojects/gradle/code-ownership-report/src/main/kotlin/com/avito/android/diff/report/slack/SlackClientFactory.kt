package com.avito.android.diff.report.slack

import Slf4jGradleLoggerFactory
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.slack.SlackClient
import com.avito.time.DefaultTimeProvider

public object SlackClientFactory {

    private val loggerFactory = Slf4jGradleLoggerFactory

    public fun create(
        slackToken: String,
        slackWorkspace: String,
    ): SlackClient = SlackClient.create(
        serviceName = "code-ownership-reporter-slack",
        token = slackToken,
        workspace = slackWorkspace,
        httpClientProvider = com.avito.http.HttpClientProvider(
            statsDSender = StatsDSender.create(StatsDConfig.Disabled, loggerFactory),
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory
        )
    )
}
