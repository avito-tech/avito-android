package com.avito.cli

import com.avito.cli.Notification.Exit
import com.avito.cli.Notification.Output
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import java.io.File

@ExperimentalCoroutinesApi
public class FlowCommandLine(command: String, args: List<String>) : CommandLine(command, args) {

    public fun start(output: File? = null): Flow<Notification> {
        return flow {
            startInternal(
                output,
                { notification ->
                    when (notification) {
                        is Output -> emit(notification)
                        is Exit -> emit(notification)
                    }
                },
                { error ->
                    throw error
                }
            )
        }.onCompletion {
            @Suppress("BlockingMethodInNonBlockingContext")
            close()
        }
    }
}
