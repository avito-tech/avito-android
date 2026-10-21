package com.avito.android.build_verdict.internal

import com.google.common.truth.Truth.assertThat
import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.ProgressCompleteEvent
import org.gradle.internal.logging.events.StyledTextOutputEvent
import org.gradle.internal.operations.OperationIdentifier
import org.junit.jupiter.api.Test

internal class GradleLogEventListenerTest {

    @Test
    fun `operation completion clears only its own KSP capture state`() {
        val firstId = OperationIdentifier(1)
        val secondId = OperationIdentifier(2)
        val firstMessages = mutableListOf<String>()
        val secondMessages = mutableListOf<String>()
        val listener = GradleLogEventListener(
            listeners = mapOf(
                firstId to collectingListener(firstMessages),
                secondId to collectingListener(secondMessages)
            )
        )
        fun stdout(id: OperationIdentifier, message: String) {
            listener.onOutput(StyledTextOutputEvent(0, "system.out", LogLevel.QUIET, id, message))
        }

        stdout(firstId, "e: [ksp] First error")
        stdout(secondId, "e: [ksp] Second error")
        listener.onOutput(ProgressCompleteEvent(firstId, 0, "", true))
        stdout(firstId, "stdout after first operation completed")
        stdout(secondId, "    second error details")
        listener.onOutput(ProgressCompleteEvent(secondId, 0, "", false))
        stdout(secondId, "stdout after second operation completed")

        assertThat(firstMessages).containsExactly("e: [ksp] First error")
        assertThat(secondMessages)
            .containsExactly("e: [ksp] Second error", "    second error details")
            .inOrder()
    }

    private fun collectingListener(messages: MutableList<String>) = object : LogMessageListener {
        override fun onLogMessage(message: String) {
            messages.add(message)
        }
    }
}
