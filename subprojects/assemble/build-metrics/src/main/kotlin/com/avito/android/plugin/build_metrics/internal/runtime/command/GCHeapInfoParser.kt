package com.avito.android.plugin.build_metrics.internal.runtime.command

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.runtime.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.MemoryUsage

internal object GCHeapInfoParser {

    fun parseHeapInfo(output: String): Result<HeapInfo> {
        val lines = output.lines()
            .map { it.trim() }

        val heapUsage = parseHeapUsage(lines)
        val metaspaceUsage = parseMetaspaceUsage(lines)

        return heapUsage.combine(metaspaceUsage) { heap, metaspace ->
            HeapInfo(heap = heap, metaspace = metaspace)
        }
    }

    private fun parseMetaspaceUsage(output: List<String>): Result<MemoryUsage> {
        val metaspaceLine = output.find { it.startsWith("Metaspace ") }
            ?: return Result.Failure(
                RuntimeException("Expected to find `Metaspace` info in GC.heap_info output:\n$output")
            )

        return parseMemoryUsageLine(
            prefix = "Metaspace ",
            output = metaspaceLine
        )
    }

    private fun parseHeapUsage(output: List<String>): Result<MemoryUsage> {
        val g1heapLine = output.find { it.startsWith("garbage-first heap ") }
        if (g1heapLine != null) {
            return parseMemoryUsageLine(
                prefix = "garbage-first heap ",
                output = g1heapLine,
                committedMetricName = "total"
            )
        }
        val youngGenLine = output.find { it.startsWith("PSYoungGen ") }
        val parOldGenLine = output.find { it.startsWith("ParOldGen ") }
        if (youngGenLine != null && parOldGenLine != null) {
            val youngGenUsage = parseMemoryUsageLine(
                prefix = "PSYoungGen ",
                output = youngGenLine,
                committedMetricName = "total"
            )
            val oldGenUsage = parseMemoryUsageLine(
                prefix = "ParOldGen ",
                output = parOldGenLine,
                committedMetricName = "total"
            )
            return youngGenUsage.combine(oldGenUsage) { young, old -> young + old }
        }
        return Result.Failure(
            RuntimeException("Unexpected GC output: ${output.joinToString()}")
        )
    }

    /**
     * @param output - "<prefix> used 219993K, capacity 246054K, ..."
     */
    private fun parseMemoryUsageLine(
        prefix: String = "",
        output: String,
        usedMetricName: String = "used",
        committedMetricName: String = "committed",
    ): Result<MemoryUsage> = Result.tryCatch {

        val metrics = output
            .removePrefix(prefix)
            .substringBefore(" [0x")
            .split(',').map { it.trim() }

        val metricToSize: Map<String, Int> = metrics.associate { metric ->
            val parts = metric.split(' ').map { it.trim() }

            val name = parts[0]
            val value = parts[1]

            check(value.endsWith('K')) {
                "Unsupported measurement unit in $output for $metric"
            }
            name to value.removeSuffix("K").toInt()
        }
        val used = requireNotNull(metricToSize[usedMetricName]) {
            "Expected 'used' in $output"
        }
        val committed = requireNotNull(metricToSize[committedMetricName]) {
            "Expected 'committed' in $output"
        }
        MemoryUsage(usedKb = used, committedKb = committed)
    }
}
