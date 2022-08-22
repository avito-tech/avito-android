package com.avito.notification.finder

import com.avito.android.stats.StubStatsdSender
import com.avito.http.HttpClientProvider
import com.avito.logger.PrintlnLoggerFactory
import com.avito.notification.NotificationClientFactory
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.time.DefaultTimeProvider
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SlackNotificationClientTest {

    @Disabled("use only locally when test slack integration")
    @Test
    fun `send message`() {
        NotificationClientFactory.createSlackClient(
            serviceName = "test-summary-slack",
            token = "TODO paste token you want to test",
            workspace = "avito",
            httpClientProvider = HttpClientProvider(
                statsDSender = StubStatsdSender(),
                timeProvider = DefaultTimeProvider(),
                loggerFactory = PrintlnLoggerFactory
            )
        ).sendMessage(
            SlackSendMessageRequest(
                channel = SlackChannel("C01D88JT6CX", "#android-speed-test-integration"),
                text = "test",
                author = "Test Analyzer",
            )
        )
    }
}
