package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ConjunctionMessageUpdateConditionTest {

    private object TrueCondition : SlackMessageUpdateCondition {
        override fun updateIf(existingMessage: FoundMessage): Boolean = true
    }

    @Test
    fun `conjunction - returns true - for multiple true conditions`() {
        val irrelevantMessage = FoundMessage.createStubInstance()

        val result = ConjunctionMessageUpdateCondition(listOf(TrueCondition, TrueCondition, TrueCondition))
            .updateIf(irrelevantMessage)

        assertThat(result).isTrue()
    }
}
