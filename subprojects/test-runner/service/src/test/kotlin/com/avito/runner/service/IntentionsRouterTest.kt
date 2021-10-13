package com.avito.runner.service

import com.avito.logger.PrintlnLoggerFactory
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.model.intention.createStubInstance
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class IntentionsRouterTest {

    private lateinit var router: IntentionsRouter

    private val loggerFactory = PrintlnLoggerFactory

    @BeforeEach
    fun setup() {
        router = IntentionsRouter(loggerFactory = loggerFactory)
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
            Intention.createStubInstance(
                state = api20State,
                action = InstrumentationTestRunAction.createStubInstance()
            ),
            Intention.createStubInstance(
                state = api20State,
                action = InstrumentationTestRunAction.createStubInstance()
            )
        )
        val expected21Intentions = listOf(
            Intention.createStubInstance(
                state = api21State,
                action = InstrumentationTestRunAction.createStubInstance()
            ),
            Intention.createStubInstance(
                state = api21State,
                action = InstrumentationTestRunAction.createStubInstance()
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

        router.cancel()

        assertThat(results20).isEqualTo(expected20Intentions)
        assertThat(results21).isEqualTo(expected21Intentions)
    }

    @Test
    fun `all layers except api level do not affect routing`() = runBlocking {
        val api20StateWithInstalledApplications = State(
            layers = listOf(
                State.Layer.ApiLevel(20),
                State.Layer.InstalledApplication.createStubInstance(),
                State.Layer.InstalledApplication.createStubInstance()
            )
        )
        val api20StateWithoutInstalledApplications = State(
            layers = listOf(
                State.Layer.ApiLevel(20)
            )
        )

        val expectedIntentions = listOf(
            Intention.createStubInstance(
                state = api20StateWithInstalledApplications,
                action = InstrumentationTestRunAction.createStubInstance()
            ),
            Intention.createStubInstance(
                state = api20StateWithInstalledApplications,
                action = InstrumentationTestRunAction.createStubInstance()
            ),
            Intention.createStubInstance(
                state = api20StateWithoutInstalledApplications,
                action = InstrumentationTestRunAction.createStubInstance()
            ),
            Intention.createStubInstance(
                state = api20StateWithoutInstalledApplications,
                action = InstrumentationTestRunAction.createStubInstance()
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

        router.cancel()

        assertThat(api20StateWithInstalledApplicationsIntentionsResults)
            .isEqualTo(expectedIntentions)
    }
}
