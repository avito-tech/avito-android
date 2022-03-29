package com.avito.cli

import com.avito.cli.CommandLine.Notification.Internal.Error
import com.avito.cli.CommandLine.Notification.Public.Exit
import com.avito.cli.CommandLine.Notification.Public.Output
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import java.io.File

@ExperimentalCoroutinesApi
public fun CommandLine.executeAsFlow(
    output: File? = null
): Flow<CommandLine.Notification.Public> = flow {
    startSuspend(output) { notification ->
        when (notification) {
            is Output -> emit(notification)
            is Error -> throw notification.error
            is Exit -> emit(notification)
        }
    }
}.onCompletion {
    @Suppress("BlockingMethodInNonBlockingContext")
    close()
}
