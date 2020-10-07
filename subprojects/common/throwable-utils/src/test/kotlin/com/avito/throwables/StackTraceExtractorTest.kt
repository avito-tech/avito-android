package com.avito.throwables

import com.avito.utils.getStackTraceString
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StackTraceExtractorTest {

    @Test
    fun `stacktrace extracted from exception contains message`() {
        assertThat(Exception("message").getStackTraceString())
            .contains("java.lang.Exception: message")
    }
}
