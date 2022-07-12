package com.avito.runner.service

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

public class IntentionsRouter(
    private val intentionRoutings: MutableMap<String, Channel<Intention>> = mutableMapOf(),
    loggerFactory: LoggerFactory
) {

    private val isObserving = AtomicBoolean(false)

    private val logger = loggerFactory.create<IntentionsRouter>()

    public fun observeIntentions(state: State): ReceiveChannel<Intention> {

        val id = state.routingIdentifier()

        logger.debug("observing intentions with id: $id for state: $state")

        val result = intentionRoutings.getOrPut(
            key = id,
            defaultValue = { Channel(Channel.UNLIMITED) }
        )

        isObserving.set(true)
        return result
    }

    public suspend fun sendIntention(intention: Intention) {

        val intentionId = intention.state.routingIdentifier()

        logger.debug("sending intention with id: $intentionId [$intention]")

        while (!isObserving.get()) {
            logger.debug("Intention router is not being observed yet, waiting...")
            delay(5)
        }

        intentionRoutings.getOrPut(
            key = intentionId,
            defaultValue = { Channel(Channel.UNLIMITED) }
        ).send(
            element = intention
        )
    }

    public fun cancel() {
        intentionRoutings.forEach { (_, channel) -> channel.cancel() }
        intentionRoutings.clear()
    }

    private fun State.routingIdentifier(): String = State(
        layers = layers.filterIsInstance<State.Layer.Model>() + layers.filterIsInstance<State.Layer.ApiLevel>()
    ).digest
}
