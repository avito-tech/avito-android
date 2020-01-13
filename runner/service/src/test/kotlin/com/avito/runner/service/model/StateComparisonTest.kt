package com.avito.runner.service.model

import com.avito.runner.service.model.intention.State
import com.avito.runner.test.generateInstalledApplicationLayer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StateComparisonTest {

    @Test
    fun `states with same layers have same digests`() {
        val layers = listOf(
            State.Layer.ApiLevel(22),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer()
        )
        val state = State(
            layers = layers
        )
        val theSameState = State(layers = layers)

        assertThat(state.digest).isEqualTo(theSameState.digest)
    }

    @Test
    fun `states with different layers have different digests`() {
        val state = State(
            layers = listOf(
                State.Layer.ApiLevel(22),
                generateInstalledApplicationLayer(applicationPackage = "package 1"),
                generateInstalledApplicationLayer(applicationPackage = "package 1"),
                generateInstalledApplicationLayer(applicationPackage = "package 1")
            )
        )
        val stateWithAnotherApiLayer = State(
            layers = listOf(
                State.Layer.ApiLevel(22),
                generateInstalledApplicationLayer(applicationPackage = "package 2"),
                generateInstalledApplicationLayer(applicationPackage = "package 2"),
                generateInstalledApplicationLayer(applicationPackage = "package 2")
            )
        )

        assertThat(state.digest).isNotEqualTo(stateWithAnotherApiLayer.digest)
    }

    @Test
    fun `states with same layers are equal`() {
        val layers = listOf(
            State.Layer.ApiLevel(22),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer(),
            generateInstalledApplicationLayer()
        )
        val state = State(layers = layers)
        val theSameState = State(layers = layers)

        assertThat(state).isEqualTo(theSameState)
    }
}
