package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class ApktoolRunner(
    private val apktoolJar: File,
    private val javaExecutable: String,
    private val decodeTimeout: Duration = Duration.ofMinutes(5),
    private val buildTimeout: Duration = Duration.ofMinutes(20),
) {

    fun decode(inputApk: File, workspaceDirectory: File): Result<Unit> {
        return run(
            phase = "decode",
            timeout = decodeTimeout,
            arguments = listOf(
                javaExecutable,
                "-jar",
                apktoolJar.absolutePath,
                "d",
                inputApk.absolutePath,
                "-o",
                workspaceDirectory.absolutePath,
                "--force",
            ),
        )
    }

    fun build(workspaceDirectory: File, outputApk: File): Result<Unit> {
        return run(
            phase = "build",
            timeout = buildTimeout,
            arguments = listOf(
                javaExecutable,
                "-jar",
                apktoolJar.absolutePath,
                "b",
                workspaceDirectory.absolutePath,
                "-o",
                outputApk.absolutePath,
            ),
        )
    }

    private fun run(
        phase: String,
        timeout: Duration,
        arguments: List<String>,
    ): Result<Unit> = Result.tryCatch {
        check(apktoolJar.isFile) {
            "apktool jar is not available: ${apktoolJar.path}"
        }

        withTempOutputFile(phase) { outputFile ->
            val process = ProcessBuilder(arguments)
                .redirectErrorStream(true)
                .redirectOutput(outputFile)
                .start()

            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                error("apktool $phase timed out after ${timeout.seconds}s: ${outputFile.readText()}")
            }

            val output = outputFile.readText()
            check(process.exitValue() == 0) {
                "apktool $phase failed (exit code ${process.exitValue()}): $output"
            }
        }
    }

    private inline fun <T> withTempOutputFile(phase: String, block: (File) -> T): T {
        val outputFile = File.createTempFile("apktool-$phase-", ".log")
        return try {
            block(outputFile)
        } finally {
            outputFile.delete()
        }
    }
}
