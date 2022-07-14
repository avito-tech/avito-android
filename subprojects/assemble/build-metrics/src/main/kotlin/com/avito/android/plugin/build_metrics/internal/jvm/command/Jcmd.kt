package com.avito.android.plugin.build_metrics.internal.jvm.command

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.jvm.GCHeapInfoParser
import com.avito.android.plugin.build_metrics.internal.jvm.HeapInfo
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

internal class Jcmd(
    private val processRunner: ProcessRunner,
    javaHome: File = javaHome(),
    private val timeout: Duration = Duration.ofSeconds(5),
) {

    private val jcmd = File(javaHome, "bin/jcmd").path

    fun gcHeapInfo(pid: Long): Result<HeapInfo> {
        val result = processRunner.run("$jcmd $pid GC.heap_info", timeout)
        if (result.isFailure()) {
            return Result.Failure((result as Result.Failure).throwable)
        }
        return GCHeapInfoParser.parseHeapInfo(result.getOrThrow())
    }
}
