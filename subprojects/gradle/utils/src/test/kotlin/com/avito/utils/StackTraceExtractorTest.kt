package com.avito.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StackTraceExtractorTest {

    @Test
    fun `stacktrace extracted from exception contains message`() {
        assertThat(Exception("message").getStackTraceString())
            .contains("java.lang.Exception: message")
    }
}
