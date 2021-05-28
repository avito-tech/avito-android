package com.avito.utils

import com.avito.android.Result
import org.apache.tools.ant.types.Commandline
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface ProcessRunner {

    /**
     * @param command i.e. ls -la
     *
     * @return output команды, если exit code = 0
     */
    fun run(command: String, timeout: Duration): Result<String>

    fun spawn(command: String, outputTo: File?): Process

    class Real(
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
        fun splitCommand(source: String): Array<String> = Commandline.translateCommandline(source)

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

            process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
            val output = outputFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS)

            if (process.exitValue() != 0) {
                Result.Failure(Throwable("Unknown error: exit code=[${process.exitValue()}]; output=$output"))
            } else {
                Result.Success(output)
            }
        } catch (t: Throwable) {
            Result.Failure(t)
        }
    }
}

private class CLIOutputReaderTask(
    private val stdout: InputStream
) : Callable<String> {

    override fun call(): String {
        val result = StringBuilder()
        val stdoutBuffer = BufferedReader(InputStreamReader(stdout))
        while (true) {
            val line = stdoutBuffer.readLine()
            if (line != null) {
                result.appendLine(line)
            } else {
                break
            }
        }
        return result.toString().trim()
    }
}
