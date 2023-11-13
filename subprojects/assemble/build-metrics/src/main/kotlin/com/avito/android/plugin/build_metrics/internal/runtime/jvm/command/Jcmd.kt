package com.avito.android.plugin.build_metrics.internal.runtime.jvm.command

import com.avito.android.Result
import com.avito.android.isFailure
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JavaHome
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

internal class Jcmd(
    private val processRunner: ProcessRunner,
    private val javaHome: JavaHome,
    private val timeout: Duration = Duration.ofSeconds(5),
) {

    private val jcmd = File(javaHome.path, "bin/jcmd").path

    fun gcHeapInfo(pid: Long): Result<HeapInfo> {
        if (!javaHome.isJdk) {
            return Result.Failure(
                RuntimeException("Missing JDK binaries. Most probably you use JRE instead of JDK.")
            )
        }
        val result = processRunner.run("$jcmd $pid GC.heap_info", timeout)
        if (result.isFailure()) {
            return Result.Failure(result.throwable)
        }
        return GCHeapInfoParser.parseHeapInfo(result.getOrThrow())
    }
}
