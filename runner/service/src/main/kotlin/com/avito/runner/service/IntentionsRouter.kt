package com.avito.runner.service

import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

class IntentionsRouter(
    private val intentionRoutings: MutableMap<String, Channel<Intention>> = mutableMapOf()
) {

    fun observeIntentions(state: State): ReceiveChannel<Intention> =
        intentionRoutings.getOrPut(
            key = state.routingIdentifier(),
            defaultValue = { Channel(Channel.UNLIMITED) }
        )

    suspend fun sendIntention(intention: Intention) {
        intentionRoutings.getOrPut(
            key = intention.state.routingIdentifier(),
            defaultValue = { Channel(Channel.UNLIMITED) }
        ).send(
            element = intention
        )
    }

    fun close() {
        intentionRoutings.forEach { (_, channel) -> channel.close() }
        intentionRoutings.clear()
    }

    private fun State.routingIdentifier(): String = State(
        layers = layers.filterIsInstance<State.Layer.ApiLevel>()
    )
        .digest
}
