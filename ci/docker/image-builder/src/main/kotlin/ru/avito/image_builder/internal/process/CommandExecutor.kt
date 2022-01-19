package ru.avito.image_builder.internal.process

import java.io.File
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class CommandExecutor(
    private val workingDirectory: File? = null,
    private val outputReaderThreadPool: ExecutorService =
        ThreadPoolExecutor(0, 10, 5, TimeUnit.SECONDS, SynchronousQueue())
) {

    fun run(
        arguments: List<String>,
        timeout: Duration
    ): Result<String> {
        return runCommand(
            arguments = arguments,
            workingDirectory = workingDirectory,
            timeout = timeout
        )
    }

    private fun runCommand(
        arguments: List<String>,
        workingDirectory: File? = null,
        timeout: Duration
    ): Result<String> = try {
        val process = spawnProcess(
            arguments = arguments,
            workingDirectory = workingDirectory
        )
        val outputFuture = outputReaderThreadPool.submit(
            CLIOutputReaderTask(
                stdout = process.inputStream
            )
        )

        val timeoutMs = timeout.toMillis()
        process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)

        val output = outputFuture.get(timeoutMs, TimeUnit.MILLISECONDS)

        try {
            if (process.exitValue() != 0) {
                Result.failure(Throwable("Unknown error: exit code=[${process.exitValue()}]; output=$output"))
            } else {
                Result.success(output)
            }
        } catch (e: IllegalThreadStateException) {
            Result.failure(Throwable("Process didn't exit in ${timeoutMs}ms; output=$output"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    private fun spawnProcess(
        arguments: List<String>,
        workingDirectory: File? = null,
    ): Process {
        return ProcessBuilder(arguments)
            .redirectErrorStream(true)
            .also { builder ->
                if (workingDirectory != null) {
                    builder.directory(workingDirectory)
                }
            }
            .start()
    }
}
