package com.avito.android.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StackTraceFormatterKtTest {

    @Test
    fun `formatStackTrace result - contains first two lines of exception stacktrace`() {
        val exception = IllegalStateException("Something went wrong")
        val result: List<String> = exception.formatStackTrace()

        assertThat(result).containsAtLeast(
            "java.lang.IllegalStateException: Something went wrong",
            "\tat com.avito.android.util.StackTraceFormatterKtTest.formatStackTrace result - contains first two lines of exception stacktrace(StackTraceFormatterKtTest.kt:10)"
        ).inOrder()
    }
}
