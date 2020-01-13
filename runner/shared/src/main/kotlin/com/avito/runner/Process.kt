package com.avito.runner

import rx.Emitter
import rx.Observable
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Random
import java.util.concurrent.TimeUnit

sealed class ProcessNotification {
    object Start : ProcessNotification()
    data class Exit(val output: String) : ProcessNotification()
    data class Output(val line: String) : ProcessNotification()
}

fun process(
    commandAndArgs: List<String>,
    redirectOutputTo: File? = null
): Observable<ProcessNotification> = Observable.create(
    { emitter ->
        val outputFile: File = when {
            redirectOutputTo == null || redirectOutputTo.isDirectory -> {
                prepareOutputFile(redirectOutputTo, true)
            }
            else -> redirectOutputTo
        }
        outputFile.apply { parentFile?.mkdirs() }

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
        process.waitFor()

        val code = process.exitValue()

        when (code) {
            0 -> {
                emitter.onNext(ProcessNotification.Exit(buffer.toString()))
                emitter.onCompleted()
            }
            else -> {
                emitter.onError(IllegalStateException("Process $commandAndArgs exited with non-zero code $code"))
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

fun Long.millisecondsToHumanReadableTime(): String {
    var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(this)
    var minutes: Long = (seconds / 60).apply {
        seconds -= this * 60
    }
    val hours: Long = (minutes / 60).apply {
        minutes -= this * 60
    }

    return buildString {
        if (hours != 0L) {
            append("$hours hour")

            if (hours > 1) {
                append("s")
            }

            append(" ")
        }

        if (minutes != 0L || hours > 0) {
            append("$minutes minute")

            if (minutes != 1L) {
                append("s")
            }

            append(" ")
        }

        append("$seconds second")

        if (seconds != 1L) {
            append("s")
        }
    }
}
