package com.avito.runner.service.model

import com.avito.runner.service.model.intention.State
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class IntentionComparisonTest {

    @Test
    fun `intentions are equal when have the same state and action`() {
        val sharedAction = generateInstrumentationTestAction()

        val layers = listOf(
            State.Layer.ApiLevel(22),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer()
        )

        val firstIntention = generateIntention(
            state = State(layers = layers),
            action = sharedAction.copy()
        )
        val theSameIntention = generateIntention(
            state = State(layers = layers),
            action = sharedAction.copy()
        )

        assertThat(firstIntention.state).isEqualTo(theSameIntention.state)
        assertThat(firstIntention.hashCode()).isEqualTo(theSameIntention.hashCode())
    }

    @Test
    fun `intentions are different when have different layers`() {
        val sharedAction = generateInstrumentationTestAction()

        val firstIntention = generateIntention(
            state = State(
                layers = listOf(
                    State.Layer.ApiLevel(22),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer()
                )
            ),
            action = sharedAction
        )
        val intentionWithAnotherLayer = generateIntention(
            state = State(
                layers = listOf(
                    State.Layer.ApiLevel(23),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer()
                )
            ),
            action = sharedAction
        )

        assertThat(firstIntention).isNotEqualTo(intentionWithAnotherLayer)
    }
}
