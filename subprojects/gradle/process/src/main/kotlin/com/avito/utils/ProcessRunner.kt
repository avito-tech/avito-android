package com.avito.utils

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import org.apache.tools.ant.types.Commandline
import org.funktionale.tries.Try
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

interface ProcessRunner {

    /**
     * @param command например ls -la
     *
     * @return output команды, если exit code = 0
     */
    fun run(command: String): Try<String>

    class Real(
        private val workingDirectory: File?,
        private val loggerFactory: LoggerFactory
    ) : ProcessRunner {

        override fun run(command: String): Try<String> = runCommand(
            command = command,
            workingDirectory = workingDirectory,
            loggerFactory = loggerFactory
        )
    }
}

/**
 * processBuilder ожидает команду списком аргументов
 * разделение не самое тривиальное, т.к. поддерживаются аргументы с пробелами
 */
fun splitCommand(source: String): Array<String> = Commandline.translateCommandline(source)

fun spawnProcess(
    command: String,
    workingDirectory: File? = null,
    outputTo: File? = null
): Process {
    return ProcessBuilder(
        *splitCommand(command)
    )
        .apply {
            if (workingDirectory != null) {
                directory(workingDirectory)
            }

            if (outputTo != null) {
                redirectOutput(outputTo)
            }
        }
        .start()
}

@JvmOverloads
fun runCommand(
    command: String,
    workingDirectory: File? = null,
    loggerFactory: LoggerFactory
): Try<String> = try {
    val binary = splitCommand(command).firstOrNull()

    val logger = loggerFactory.create(binary ?: "runCommand")

    val process = spawnProcess(
        command = command,
        workingDirectory = workingDirectory
    )

    val processOutput = StringBuffer()

    val outputThread = IOThread(
        source = process.inputStream,
        result = processOutput,
        logger = logger
    )

    val errorThread = IOThread(
        source = process.errorStream,
        result = processOutput,
        logger = logger
    )

    outputThread.start()
    errorThread.start()

    outputThread.join()
    errorThread.join()
    process.waitFor()

    val output = processOutput.toString().trim()

    if (process.exitValue() != 0) {
        Try.Failure(Throwable("Unknown error: exit code=[${process.exitValue()}]; output=$output"))
    } else {
        Try.Success(output)
    }
} catch (t: Throwable) {
    Try.Failure(t)
}

private class IOThread(
    val source: InputStream,
    val result: StringBuffer,
    val logger: Logger
) : Thread() {

    init {
        isDaemon = true
    }

    override fun run() {
        val reader = BufferedReader(InputStreamReader(source))

        var line: String? = reader.readLine()
        while (line != null) {
            result.appendLine(line)

            logger.debug(line)

            line = reader.readLine()
        }
    }
}
