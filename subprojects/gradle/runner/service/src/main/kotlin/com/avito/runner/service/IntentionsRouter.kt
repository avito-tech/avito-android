package com.avito.runner.service

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

class IntentionsRouter(
    private val intentionRoutings: MutableMap<String, Channel<Intention>> = mutableMapOf(),
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<IntentionsRouter>()

    fun observeIntentions(state: State): ReceiveChannel<Intention> {

        val id = state.routingIdentifier()

        logger.debug("observing intentions with id: $id for state: $state")

        return intentionRoutings.getOrPut(
            key = id,
            defaultValue = { Channel(Channel.UNLIMITED) }
        )
    }

    suspend fun sendIntention(intention: Intention) {

        val intentionId = intention.state.routingIdentifier()

        logger.debug("sending intention with id: $intentionId [$intention]")

        intentionRoutings.getOrPut(
            key = intentionId,
            defaultValue = { Channel(Channel.UNLIMITED) }
        ).send(
            element = intention
        )
    }

    fun cancel() {
        intentionRoutings.forEach { (_, channel) -> channel.cancel() }
        intentionRoutings.clear()
    }

    private fun State.routingIdentifier(): String = State(
        layers = layers.filterIsInstance<State.Layer.Model>() + layers.filterIsInstance<State.Layer.ApiLevel>()
    ).digest
}
