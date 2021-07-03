package com.avito.runner

import rx.Observable
import java.io.File

public sealed class ProcessNotification {
    public object Start : ProcessNotification()
    public data class Exit(val output: String) : ProcessNotification()
    public data class Output(val line: String) : ProcessNotification()
}

public interface CommandLineExecutor {

    public fun executeProcess(
        command: String,
        args: List<String> = emptyList(),
        output: File? = null
    ): Observable<ProcessNotification>

    public companion object {

        public fun create(): CommandLineExecutor = CommandLineExecutorImpl()
    }
}
