package com.avito.runner.service.model

import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.model.intention.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class IntentionComparisonTest {

    @Test
    fun `intentions are equal when have the same state and action`() {
        val sharedAction = InstrumentationTestRunAction.createStubInstance()

        val layers = listOf(
            State.Layer.ApiLevel(22),
            State.Layer.InstalledApplication.createStubInstance(),
            State.Layer.InstalledApplication.createStubInstance(),
            State.Layer.InstalledApplication.createStubInstance()
        )

        val firstIntention = Intention.createStubInstance(
            state = State(layers = layers),
            action = sharedAction.copy()
        )
        val theSameIntention = Intention.createStubInstance(
            state = State(layers = layers),
            action = sharedAction.copy()
        )

        assertThat(firstIntention.state).isEqualTo(theSameIntention.state)
        assertThat(firstIntention.hashCode()).isEqualTo(theSameIntention.hashCode())
    }

    @Test
    fun `intentions are different when have different layers`() {
        val sharedAction = InstrumentationTestRunAction.createStubInstance()

        val firstIntention = Intention.createStubInstance(
            state = State(
                layers = listOf(
                    State.Layer.ApiLevel(22),
                    State.Layer.InstalledApplication.createStubInstance(),
                    State.Layer.InstalledApplication.createStubInstance(),
                    State.Layer.InstalledApplication.createStubInstance()
                )
            ),
            action = sharedAction
        )
        val intentionWithAnotherLayer = Intention.createStubInstance(
            state = State(
                layers = listOf(
                    State.Layer.ApiLevel(23),
                    State.Layer.InstalledApplication.createStubInstance(),
                    State.Layer.InstalledApplication.createStubInstance(),
                    State.Layer.InstalledApplication.createStubInstance()
                )
            ),
            action = sharedAction
        )

        assertThat(firstIntention).isNotEqualTo(intentionWithAnotherLayer)
    }
}
