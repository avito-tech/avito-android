package com.avito.android.plugin.build_metrics.internal.runtime.command

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.runtime.JavaHome
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

internal class Jps(
    private val processRunner: ProcessRunner,
    private val javaHome: JavaHome,
    private val timeout: Duration = Duration.ofSeconds(5)
) {

    private val jps = File(javaHome.path, "bin/jps").path

    fun run(): Result<Set<LocalVm.Unknown>> {
        if (!javaHome.isJdk) {
            return Result.Failure(
                RuntimeException("Missing JDK binaries. Most probably you use JRE instead of JDK.")
            )
        }

        return processRunner.run("$jps -l", timeout)
            .map { output ->
                output.lines()
                    .filter { it.isNotBlank() }
                    .map { parse(it) }
                    .toSet()
            }
    }

    private fun parse(line: String): LocalVm.Unknown {
        val parts = line.split(' ')
        check(parts.isNotEmpty()) {
            "Expected to have at least id in jps output: $line"
        }
        return when (parts.size) {
            // If the target JVM is started with a custom launcher
            1 -> LocalVm.Unknown(id = parts[0].toLong(), name = "Unknown")
            else -> LocalVm.Unknown(id = parts[0].toLong(), name = parts[1])
        }
    }
}
