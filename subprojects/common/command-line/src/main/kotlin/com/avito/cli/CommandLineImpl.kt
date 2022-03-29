package com.avito.cli

import com.avito.cli.CommandLine.Notification.Internal.Error
import com.avito.cli.CommandLine.Notification.Public.Exit
import com.avito.cli.CommandLine.Notification.Public.Output
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Random

internal class CommandLineImpl(
    private val command: String,
    args: List<String>
) : CommandLine {

    private val commandAndArgs = listOf(command) + args

    private val process: Process by lazy {
        ProcessBuilder(commandAndArgs)
            .redirectErrorStream(true)
            .start()
    }

    override fun close() {
        process.destroy()
    }

    override fun start(
        output: File?,
        listener: (CommandLine.Notification) -> Unit
    ) {
        startInternal(output) { notification ->
            listener(notification)
        }
    }

    override suspend fun startSuspend(
        output: File?,
        listener: suspend (CommandLine.Notification) -> Unit
    ) {
        startInternal(output) { notification ->
            listener(notification)
        }
    }

    private inline fun startInternal(
        output: File?,
        listener: (CommandLine.Notification) -> Unit
    ) {
        val outputFile = prepareOutputFile(output)
        val reader = BufferedReader(
            InputStreamReader(process.inputStream)
        )

        val buffer = StringBuffer()
        var line: String? = reader.readLine()
        while (line != null) {
            listener(Output(line = line))
            buffer.appendLine(line)
            outputFile?.appendText("$line${System.lineSeparator()}")
            line = reader.readLine()
        }

        reader.close()

        when (val exitCode = process.waitFor()) {
            0 -> listener(Exit(buffer.toString()))
            else ->
                listener(
                    Error(
                        IllegalStateException(
                            "Process $commandAndArgs exited with non-zero code $exitCode. " +
                                "Output: $buffer"
                        )
                    )
                )
        }
    }

    private fun prepareOutputFile(
        output: File?
    ): File? {
        val resultFile = when {
            output == null -> null
            output.isDirectory -> createOutputFile(output)
            else -> output
        }
        if (resultFile != null) {
            resultFile.parentFile?.mkdirs()
        }
        return resultFile
    }

    private fun createOutputFile(dir: File): File {
        val name = Random().nextInt()
        return File(dir, "$command#$name.output").apply {
            createNewFile()
        }
    }
}
