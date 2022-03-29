package com.avito.cli

import java.io.Closeable
import java.io.File

public interface CommandLine : Closeable {

    public sealed class Notification {
        public sealed class Public : Notification() {
            public class Output(public val line: String) : Public()
            public class Exit(public val output: String) : Public()
        }

        public sealed class Internal : Notification() {
            public class Error(public val error: Throwable) : Internal()
        }
    }

    public fun start(
        output: File?,
        listener: (Notification) -> Unit
    )

    public suspend fun startSuspend(
        output: File?,
        listener: suspend (Notification) -> Unit
    )

    public companion object {
        public fun create(
            command: String,
            args: List<String>
        ): CommandLine {
            return CommandLineImpl(command, args)
        }
    }
}
