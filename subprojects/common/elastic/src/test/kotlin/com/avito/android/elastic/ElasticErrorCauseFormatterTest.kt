package com.avito.android.elastic

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ElasticErrorCauseFormatterTest {

    @Test
    fun `null throwable - empty sting result`() {
        val result = null.formatCauseForElastic()
        assertThat(result).isEmpty()
    }

    @Test
    fun `throwable without cause - simple message`() {
        val result = Throwable(message = "SampleMessage", cause = null).formatCauseForElastic()
        assertThat(result).isEqualTo("SampleMessage")
    }

    @Test
    fun `throwable without message - explicit no message`() {
        val result = Throwable(message = "SampleMessage", cause = Throwable()).formatCauseForElastic()
        assertThat(result).isEqualTo("""
            SampleMessage
            Caused by: No message
        """.trimIndent())
    }

    @Test
    fun `chain of throwables - all messages are present`() {
        val result = Throwable(
            message = "Message1",
            cause = Throwable(
                message = "Message2",
                cause = Throwable(
                    message = "Message3"
                )
            )
        ).formatCauseForElastic()
        assertThat(result).isEqualTo("""
            Message1
            Caused by: Message2
            Caused by: Message3
        """.trimIndent())
    }
}
