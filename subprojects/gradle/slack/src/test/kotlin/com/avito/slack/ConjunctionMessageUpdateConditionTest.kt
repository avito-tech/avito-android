package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ConjunctionMessageUpdateConditionTest {

    private object TrueCondition : SlackMessagePredicate {
        override fun matches(existingMessage: FoundMessage): Boolean = true
    }

    @Test
    fun `conjunction - returns true - for multiple true conditions`() {
        val irrelevantMessage = FoundMessage.createStubInstance()

        val result = ConjunctionMessagePredicate(listOf(TrueCondition, TrueCondition, TrueCondition))
            .matches(irrelevantMessage)

        assertThat(result).isTrue()
    }
}
