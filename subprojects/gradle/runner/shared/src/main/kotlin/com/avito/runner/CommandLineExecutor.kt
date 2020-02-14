package com.avito.runner

import rx.Emitter
import rx.Observable
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Random

sealed class ProcessNotification {
    object Start : ProcessNotification()
    data class Exit(val output: String) : ProcessNotification()
    data class Output(val line: String) : ProcessNotification()
}

interface CommandLineExecutor {

    fun executeProcess(
        command: String,
        args: List<String> = emptyList(),
        output: File? = null
    ): Observable<ProcessNotification>

    class Impl : CommandLineExecutor {
        override fun executeProcess(
            command: String,
            args: List<String>,
            output: File?
        ): Observable<ProcessNotification> = Observable.create(
            { emitter ->
                val outputFile: File = when {
                    output == null || output.isDirectory -> {
                        prepareOutputFile(output, true)
                    }
                    else -> output
                }
                outputFile.apply { parentFile?.mkdirs() }

                val commandAndArgs: List<String> = listOf(command) + args
                val process: Process = ProcessBuilder(commandAndArgs)
                    .redirectErrorStream(true)
                    .start()

                emitter.setCancellation {
                    process.destroy()
                }

                emitter.onNext(ProcessNotification.Start)

                val reader = BufferedReader(
                    InputStreamReader(process.inputStream)
                )

                val buffer = StringBuffer()
                var line: String? = reader.readLine()
                while (line != null) {
                    emitter.onNext(
                        ProcessNotification.Output(line = line)
                    )
                    buffer.appendln(line)

                    outputFile.appendText("$line${System.lineSeparator()}")

                    line = reader.readLine()
                }

                reader.close()

                val exitCode = process.waitFor()

                when (exitCode) {
                    0 -> {
                        emitter.onNext(ProcessNotification.Exit(buffer.toString()))
                        emitter.onCompleted()
                    }
                    else -> {
                        emitter.onError(IllegalStateException("Process $commandAndArgs exited with non-zero code $exitCode"))
                    }
                }
            }, Emitter.BackpressureMode.ERROR
        )

        private fun prepareOutputFile(parent: File?, keepOnExit: Boolean): File = Random()
            .nextInt()
            .let { System.nanoTime() + it }
            .let { name ->
                File(parent, "$name.output").apply {
                    createNewFile()
                    if (!keepOnExit) {
                        deleteOnExit()
                    }
                }
            }
    }
}
