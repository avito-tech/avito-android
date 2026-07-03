package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit

internal data class ProcessResult(val exitCode: Int, val stderr: List<String>)

internal fun interface RmzProcessRunner {
    suspend fun run(command: List<String>): Result<ProcessResult>
}

internal object RmzProcessRunnerImpl : RmzProcessRunner {

    private const val SIGTERM_GRACE_SECONDS = 2L
    private const val MAX_STDERR_LINES = 200

    override suspend fun run(command: List<String>): Result<ProcessResult> {
        val process = try {
            withContext(Dispatchers.IO) { ProcessBuilder(command).start() }
        } catch (e: IOException) {
            return Result.Failure(e)
        }
        try {
            return runInterruptible(Dispatchers.IO) {
                process.outputStream.close()
                val stdoutDrainer = Thread { process.inputStream.use { it.copyTo(OutputStream.nullOutputStream()) } }
                    .apply { isDaemon = true; start() }
                val stderr = process.errorStream.bufferedReader().use { it.sampleLines(MAX_STDERR_LINES) }
                val exit = process.waitFor()
                stdoutDrainer.join()
                Result.Success(ProcessResult(exit, stderr))
            }
        } finally {
            if (process.isAlive) {
                process.destroy()
                if (!process.waitFor(SIGTERM_GRACE_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                }
            }
        }
    }

    private fun BufferedReader.sampleLines(max: Int): List<String> {
        val kept = ArrayList<String>(max)
        forEachLine { line ->
            if (line.isNotBlank() && kept.size < max) {
                kept += line
            }
        }
        return kept
    }
}
