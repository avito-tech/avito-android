package com.avito.android.plugin

import org.funktionale.tries.Try
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private val spaceSplit by lazy { "\\s".toRegex() }

fun String.runCommand(
    workingDirectory: File? = null
): Try<String> {
    return this.split(regex = spaceSplit).runCommand(workingDirectory)
}

fun List<String>.runCommand(
    workingDirectory: File? = null
): Try<String> {
    return try {
        val processBuilder = ProcessBuilder(this)
            .redirectErrorStream(true)
        workingDirectory?.let {
            processBuilder.directory(it)
        }
        val process = processBuilder.start()

        val processOutput = StringBuilder()

        BufferedReader(InputStreamReader(process.inputStream))
            .use { processOutputReader ->
                var readLine: String? = processOutputReader.readLine()

                while (readLine != null) {
                    processOutput.appendLine(readLine)
                    readLine = processOutputReader.readLine()
                }
                process.waitFor()
            }

        val output = processOutput.toString().trim()

        if (process.exitValue() != 0) {
            Try.Failure(Throwable("Unknown error: exit code=[${process.exitValue()}]; output=$output"))
        } else {
            Try.Success(output)
        }
    } catch (e: Exception) {
        Try.Failure(e)
    }
}
