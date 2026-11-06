package com.avito.test.summary.sender

import com.avito.logger.Logger
import com.avito.test.summary.model.CrossDeviceSuite
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestSummarySendersTest {

    private val alertinoSender = FakeSender()

    private val timeSender = FakeSender()

    private val logger = FakeLogger()

    @Test
    fun `senders - alertino and time - when time is configured`() {
        val senders = senders(timeBaseUrl = "https://time.localhost", timeToken = "token")

        assertThat(senders).containsExactly(alertinoSender, timeSender).inOrder()
    }

    @Test
    fun `senders - alertino only - when time token is not set`() {
        val senders = senders(timeBaseUrl = "https://time.localhost", timeToken = null)

        assertThat(senders).containsExactly(alertinoSender)
        assertThat(logger.warnings).isEmpty()
    }

    @Test
    fun `senders - alertino only with warning - when time base url is invalid`() {
        val senders = senders(timeBaseUrl = "not a url", timeToken = "token")

        assertThat(senders).containsExactly(alertinoSender)
        assertThat(logger.warnings).containsExactly("Cannot initialize Time sender, continuing with Alertino")
    }

    @Test
    fun `senders - alertino only with warning - when time base url is not set`() {
        val senders = senders(timeBaseUrl = null, timeToken = "token")

        assertThat(senders).containsExactly(alertinoSender)
        assertThat(logger.warnings).containsExactly("Cannot initialize Time sender, continuing with Alertino")
    }

    private fun senders(timeBaseUrl: String?, timeToken: String?) = testSummarySenders(
        alertinoSender = alertinoSender,
        timeBaseUrl = timeBaseUrl,
        timeToken = timeToken,
        timeSender = { timeSender },
        logger = logger,
    )

    private class FakeSender : TestSummarySender {

        override fun send(suite: CrossDeviceSuite, reportId: String) = Unit
    }

    private class FakeLogger : Logger {

        val warnings = mutableListOf<String>()

        override fun verbose(msg: String) = Unit

        override fun debug(msg: String) = Unit

        override fun info(msg: String) = Unit

        override fun warn(msg: String, error: Throwable?) {
            warnings += msg
        }

        override fun critical(msg: String, error: Throwable) = Unit
    }
}
