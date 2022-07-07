package com.avito.notification.finder

import com.avito.notification.model.FoundMessage
import com.avito.slack.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ConjunctionPredicateTest {

    private object TrueCondition : NotificationPredicate {
        override fun matches(existingMessage: FoundMessage): Boolean = true
    }

    @Test
    fun `conjunction - returns true - for multiple true conditions`() {
        val irrelevantMessage = FoundMessage.createStubInstance()

        val result = ConjunctionPredicate(listOf(TrueCondition, TrueCondition, TrueCondition))
            .matches(irrelevantMessage)

        assertThat(result).isTrue()
    }
}
