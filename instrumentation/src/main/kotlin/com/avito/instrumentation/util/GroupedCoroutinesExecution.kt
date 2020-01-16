package com.avito.instrumentation.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

internal class GroupedCoroutinesExecution {

    private val coroutines: MutableList<CompletionAwareCoroutine> = mutableListOf()

    @UseExperimental(ExperimentalCoroutinesApi::class)
    private val activeCoroutines: List<CompletionAwareCoroutine>
        get() = coroutines
            .filter { !it.completionChannel.isClosedForReceive }

    private val activeBlockingCoroutines: List<CompletionAwareCoroutine>
        get() = activeCoroutines
            .filter { it.blocking }

    fun <T> launch(blocking: Boolean = true, action: suspend () -> T): FutureValue<T> {
        val completionChannel = Channel<CompletionAwareCoroutine.CoroutineCompletion>()

        val future = SettableFutureValue<T>()

        val job = GlobalScope.launch {
            try {
                future.set(action())
                completionChannel.send(
                    CompletionAwareCoroutine.CoroutineCompletion.SuccessCompletion
                )
            } catch (t: Throwable) {
                completionChannel.send(
                    CompletionAwareCoroutine.CoroutineCompletion.ErrorCompletion(
                        reason = t
                    )
                )
            }
        }

        coroutines.add(
            CompletionAwareCoroutine(
                job = job,
                blocking = blocking,
                completionChannel = completionChannel
            )
        )

        return future
    }

    fun join() {
        runBlocking {
            while (true) {
                if (activeBlockingCoroutines.isEmpty()) {
                    activeCoroutines.forEach {
                        it.job.cancel()
                    }
                    break
                }

                select<Unit> {
                    activeCoroutines.forEach { coroutine ->
                        coroutine.completionChannel.onReceive { completionReason ->
                            when (completionReason) {
                                is CompletionAwareCoroutine.CoroutineCompletion.ErrorCompletion -> {
                                    throw completionReason.reason
                                }

                                is CompletionAwareCoroutine.CoroutineCompletion.SuccessCompletion -> {
                                    coroutine.completionChannel.close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class CompletionAwareCoroutine(
        val job: Job,
        val blocking: Boolean,
        val completionChannel: Channel<CoroutineCompletion>
    ) {
        sealed class CoroutineCompletion {
            class ErrorCompletion(val reason: Throwable) : CoroutineCompletion()
            object SuccessCompletion : CoroutineCompletion()
        }
    }
}

internal fun launchGroupedCoroutines(action: GroupedCoroutinesExecution.() -> Unit) {
    GroupedCoroutinesExecution().apply {
        action()
    }
        .join()
}
