package com.avito.cli

import com.avito.cli.Notification.Exit
import com.avito.cli.Notification.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

public class FlowCommandLine(
    command: String,
    args: List<String>,
) : CommandLine(
    command = command,
    args = args,
) {
    public fun start(output: File? = null): Flow<Notification> {
        return callbackFlow {
            launch {
                startInternal(
                    output = output,
                    onNotification = { notification ->
                        when (notification) {
                            is Output -> trySendBlocking(notification)
                            is Exit -> {
                                trySendBlocking(notification)
                                channel.close()
                            }
                        }
                    },
                    onError = { error ->
                        cancel(CancellationException(error))
                        throw error
                    }
                )
            }
            awaitClose {
                this@FlowCommandLine.close()
            }
        }.flowOn(Dispatchers.IO)
    }
}
