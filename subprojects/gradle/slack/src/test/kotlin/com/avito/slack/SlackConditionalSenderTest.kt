package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.createStubInstance
import com.avito.time.StubTimeProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.Calendar

internal class SlackConditionalSenderTest {

    private val timeProvider = StubTimeProvider()

    @Test
    fun `todayMessage - returns true - for today message`() {
        val existingMessage = FoundMessage.createStubInstance(timestamp = "1564200000")

        timeProvider.now = Calendar.getInstance()
            .apply {
                set(2019, Calendar.JULY, 27)
            }
            .time

        val result = TodayMessageCondition(timeProvider).matches(existingMessage)

        assertThat(result).isTrue()
    }

    @Test
    fun `todayMessage - returns true - for today message in slack ts format`() {
        val existingMessage = FoundMessage.createStubInstance(timestamp = "1564200000.000005")

        timeProvider.now = Calendar.getInstance()
            .apply {
                set(2019, Calendar.JULY, 27)
            }
            .time

        val result = TodayMessageCondition(timeProvider).matches(existingMessage)

        assertThat(result).isTrue()
    }
}
