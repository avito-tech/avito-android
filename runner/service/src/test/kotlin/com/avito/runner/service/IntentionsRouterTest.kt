package com.avito.runner.service

import com.avito.runner.service.model.intention.State
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IntentionsRouterTest {

    private lateinit var router: IntentionsRouter

    @BeforeEach
    fun setup() {
        router = IntentionsRouter()
    }

    @Test
    fun `api level state layer affects routing`() = runBlocking {
        val api20State = State(
            layers = listOf(
                State.Layer.ApiLevel(20)
            )
        )
        val api21State = State(
            layers = listOf(
                State.Layer.ApiLevel(21)
            )
        )

        val expected20Intentions = listOf(
            generateIntention(
                state = api20State,
                action = generateInstrumentationTestAction()
            ),
            generateIntention(
                state = api20State,
                action = generateInstrumentationTestAction()
            )
        )
        val expected21Intentions = listOf(
            generateIntention(
                state = api21State,
                action = generateInstrumentationTestAction()
            ),
            generateIntention(
                state = api21State,
                action = generateInstrumentationTestAction()
            )
        )

        expected20Intentions.plus(expected21Intentions).forEach {
            router.sendIntention(
                intention = it
            )
        }

        val api20Intentions = router.observeIntentions(api20State)
        val api21Intentions = router.observeIntentions(api21State)

        val results20 = listOf(
            api20Intentions.receive(),
            api20Intentions.receive()
        )
        val results21 = listOf(
            api21Intentions.receive(),
            api21Intentions.receive()
        )

        assertThat(api20Intentions.isEmpty).isTrue()
        assertThat(api21Intentions.isEmpty).isTrue()

        router.close()

        assertThat(results20).isEqualTo(expected20Intentions)
        assertThat(results21).isEqualTo(expected21Intentions)
    }

    @Test
    fun `all layers except api level do not affect routing`() = runBlocking {
        val api20StateWithInstalledApplications = State(
            layers = listOf(
                State.Layer.ApiLevel(20),
                generateInstalledApplicationLayer(),
                generateInstalledApplicationLayer()
            )
        )
        val api20StateWithoutInstalledApplications = State(
            layers = listOf(
                State.Layer.ApiLevel(20)
            )
        )

        val expectedIntentions = listOf(
            generateIntention(
                state = api20StateWithInstalledApplications,
                action = generateInstrumentationTestAction()
            ),
            generateIntention(
                state = api20StateWithInstalledApplications,
                action = generateInstrumentationTestAction()
            ),
            generateIntention(
                state = api20StateWithoutInstalledApplications,
                action = generateInstrumentationTestAction()
            ),
            generateIntention(
                state = api20StateWithoutInstalledApplications,
                action = generateInstrumentationTestAction()
            )
        )

        expectedIntentions.forEach {
            router.sendIntention(it)
        }

        val api20StateWithInstalledApplicationsIntentions = router.observeIntentions(
            api20StateWithInstalledApplications
        )
        val api20StateWithoutInstalledApplicationsIntentions = router.observeIntentions(
            api20StateWithoutInstalledApplications
        )

        val api20StateWithInstalledApplicationsIntentionsResults = listOf(
            api20StateWithInstalledApplicationsIntentions.receive(),
            api20StateWithInstalledApplicationsIntentions.receive(),
            api20StateWithInstalledApplicationsIntentions.receive(),
            api20StateWithInstalledApplicationsIntentions.receive()
        )

        assertThat(api20StateWithInstalledApplicationsIntentions.isEmpty).isTrue()
        assertThat(api20StateWithoutInstalledApplicationsIntentions.isEmpty).isTrue()

        router.close()

        assertThat(api20StateWithInstalledApplicationsIntentionsResults)
            .isEqualTo(expectedIntentions)
    }
}
