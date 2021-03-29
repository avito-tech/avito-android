package com.avito.throwables

import com.avito.utils.stackTraceToList
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StackTraceFormatterKtTest {

    @Test
    fun `formatStackTrace result - contains first two lines of exception stacktrace`() {
        val exception = IllegalStateException("Something went wrong")
        val result: List<String> = exception.stackTraceToList()

        assertThat(result).containsAtLeast(
            "java.lang.IllegalStateException: Something went wrong",
            "\tat com.avito.throwables.StackTraceFormatterKtTest.formatStackTrace result - " +
                "contains first two lines of exception stacktrace(StackTraceFormatterKtTest.kt:11)"
        ).inOrder()
    }
}
