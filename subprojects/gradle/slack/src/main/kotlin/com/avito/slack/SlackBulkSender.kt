package com.avito.slack

import com.avito.slack.model.SlackSendMessageRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Limit request frequency to avoid slack message throttling
 * https://api.slack.com/docs/rate-limits
 * > Posting messages	1 per second	short bursts >1 allowed
 */
public interface SlackBulkSender {

    public fun sendBulk(body: Bulk.() -> Unit)
}

// todo use Flow API
@OptIn(ObsoleteCoroutinesApi::class)
public class CoroutinesSlackBulkSender(
    private val sender: SlackMessageSender,
) : SlackBulkSender {

    private val requestQueue = Channel<SlackSendMessageRequest>(Channel.UNLIMITED)

    private val executedRequests = Channel<SlackSendMessageRequest>()

    private val ticker = kotlinx.coroutines.channels.ticker(delayMillis = 1500L, initialDelayMillis = 0L)

    init {
        // todo use Flow
        @Suppress("DEPRECATION")
        val tickedQueue = requestQueue.zip(ticker) { request, _ -> request }
        // TODO: Don't use global scope. Unconfined coroutines lead to leaks
        GlobalScope.launch {
            for (request in tickedQueue) {
                sender.sendMessage(request)
                    .onFailure {
                    // TODO handle failure
                    }
                executedRequests.send(request)
            }
        }
    }

    override fun sendBulk(body: Bulk.() -> Unit) {
        val bulk = Bulk(requestQueue)

        body(bulk)

        runBlocking {
            waitTillExecuted(bulk.requestCount)
        }
    }

    private suspend fun waitTillExecuted(count: Int) {
        repeat(count) {
            executedRequests.receive()
        }
    }
}

public class Bulk(private val requestQueue: Channel<SlackSendMessageRequest>) {

    public var requestCount: Int = 0

    public fun sendMessage(message: SlackSendMessageRequest) {
        requestCount++
        runBlocking {
            requestQueue.send(message)
        }
    }
}
