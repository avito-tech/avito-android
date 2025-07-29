package com.avito.android.elastic

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeoutException

class ElasticErrorCauseFormatterTest {

    @Test
    fun `null throwable - empty sting result`() {
        val result = null.formatCauseForElastic()
        assertThat(result).isEmpty()
    }

    @Test
    fun `throwable without cause - simple message`() {
        val result = Throwable(message = "SampleMessage", cause = null).formatCauseForElastic()
        assertThat(result).isEqualTo("Throwable: SampleMessage")
    }

    @Test
    fun `throwable without message - explicit no message`() {
        val result = Throwable(message = "SampleMessage", cause = Throwable()).formatCauseForElastic()
        assertThat(result).isEqualTo("""
            Throwable: SampleMessage
            Caused by Throwable with no message
        """.trimIndent())
    }

    @Test
    fun `chain of throwables - all messages are present`() {
        val result = Throwable(
            message = "Message1",
            cause = IllegalStateException(
                "Message2",
                TimeoutException(
                    "Message3"
                )
            )
        ).formatCauseForElastic()
        assertThat(result).isEqualTo("""
            Throwable: Message1
            Caused by IllegalStateException: Message2
            Caused by TimeoutException: Message3
        """.trimIndent())
    }
}
