package com.avito.utils

import com.avito.android.Result
import org.apache.tools.ant.types.Commandline
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting
import java.io.File
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class RealProcessRunner(
    private val workingDirectory: File?,
    private val cliOutputReaderThreadPool: ExecutorService =
        ThreadPoolExecutor(0, 300, 30, TimeUnit.SECONDS, SynchronousQueue())
) : ProcessRunner {

    override fun run(
        command: String,
        timeout: Duration
    ): Result<String> {
        return runCommand(
            command = command,
            workingDirectory = workingDirectory,
            timeout = timeout
        )
    }

    override fun spawn(command: String, outputTo: File?): Process {
        return spawnProcess(command, workingDirectory, outputTo)
    }

    /**
     * [ProcessBuilder] expects command as an arguments list, arguments could be separated by space
     */
    @VisibleForTesting
    internal fun splitCommand(source: String): Array<String> = Commandline.translateCommandline(source)

    private fun spawnProcess(
        command: String,
        workingDirectory: File? = null,
        outputTo: File? = null
    ): Process {
        return ProcessBuilder(*splitCommand(command))
            .redirectErrorStream(true)
            .also { builder ->
                if (workingDirectory != null) {
                    builder.directory(workingDirectory)
                }
                if (outputTo != null) {
                    builder.redirectOutput(outputTo)
                }
            }
            .start()
    }

    private fun runCommand(
        command: String,
        workingDirectory: File? = null,
        timeout: Duration
    ): Result<String> = try {
        val process = spawnProcess(
            command = command,
            workingDirectory = workingDirectory
        )
        val outputFuture = cliOutputReaderThreadPool.submit(
            CLIOutputReaderTask(
                stdout = process.inputStream
            )
        )

        val timeoutMs = timeout.toMillis()
        process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
        val output = outputFuture.get(timeoutMs, TimeUnit.MILLISECONDS)

        try {
            if (process.exitValue() != 0) {
                Result.Failure(Throwable("Unknown error: exit code=[${process.exitValue()}]; output=$output"))
            } else {
                Result.Success(output)
            }
        } catch (e: IllegalThreadStateException) {
            Result.Failure(Throwable("Process didn't exit in ${timeoutMs}ms; output=$output"))
        }
    } catch (t: Throwable) {
        Result.Failure(t)
    }
}
