package com.avito.cli

import com.avito.cli.Notification.Exit
import com.avito.cli.Notification.Output
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Random

public abstract class CommandLine(
    private val command: String,
    args: List<String>
) {

    protected val commandAndArgs: List<String> = listOf(command) + args

    protected val process: Process by lazy {
        ProcessBuilder(commandAndArgs)
            .redirectErrorStream(true)
            .start()
    }

    protected fun close() {
        process.destroy()
    }

    protected inline fun startInternal(
        output: File?,
        onNotification: (Notification) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val outputFile = prepareOutputFile(output)
        val reader = BufferedReader(
            InputStreamReader(process.inputStream)
        )

        val buffer = StringBuffer()
        var line: String? = reader.readLine()
        while (line != null) {
            onNotification(Output(line = line))
            buffer.appendLine(line)
            outputFile?.appendText("$line${System.lineSeparator()}")
            line = reader.readLine()
        }

        reader.close()

        when (val exitCode = process.waitFor()) {
            0 -> onNotification(Exit(buffer.toString()))
            else ->
                onError(
                    IllegalStateException(
                        "Process $commandAndArgs exited with non-zero code $exitCode. " +
                            "Output: $buffer"
                    )
                )
        }
    }

    protected fun prepareOutputFile(
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
